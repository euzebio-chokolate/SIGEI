package ac.gov.br.sigei_backend.service.importacao;

import ac.gov.br.sigei_backend.dto.response.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.utils.ColunaPlanilha;
import ac.gov.br.sigei_backend.utils.CoordenadaUtils;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import ac.gov.br.sigei_backend.utils.ImportacaoContador;
import ac.gov.br.sigei_backend.utils.LayoutPlanilha;
import ac.gov.br.sigei_backend.utils.LayoutPlanilha.PlanilhaLocalizada;
import ac.gov.br.sigei_backend.utils.OpcoesInfraestrutura;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Importa o cadastro das escolas com infraestrutura e equipe de um ano. Cada linha é
 * uma escola; município, terra indígena, povo e aldeia ausentes são criados.
 *
 * <p>As colunas são localizadas pelo cabeçalho ({@link Coluna}). Colunas opcionais
 * ausentes não alteram os dados já gravados. O anexo é indicado pela coluna
 * {@code TIPO DE UNIDADE} ou, no formato antigo, por {@code ANEXO} na coluna de ordem;
 * as sedes são processadas antes, para que os anexos encontrem a sede pelo código.
 */
@Service
public class ImportacaoExcelService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoExcelService.class);

    private static final int ANO_MINIMO = 2000;

    private static final ZoneId FUSO = ZoneId.of("America/Rio_Branco");

    public enum Coluna implements ColunaPlanilha {
        ORDEM("ORD.", false,
                "Número de ordem. O valor ANEXO indica um anexo quando não há a coluna TIPO DE UNIDADE.",
                "ORDEM"),
        TIPO_UNIDADE("TIPO DE UNIDADE", false,
                "SEDE ou ANEXO. O anexo é vinculado à sede de mesmo código.",
                "TIPO UNIDADE"),
        MUNICIPIO("MUNICÍPIO", true, "Nome do município."),
        CODIGO("CÓDIGO", true, "Código INEP da escola.", "CÓDIGO INEP", "INEP"),
        ESCOLA("NOME DA ESCOLA", true, "Nome da escola.", "ESCOLA"),
        TERRA_INDIGENA("TERRA INDÍGENA", true, "Nome da terra indígena."),
        ALDEIA("ALDEIA", true, "Nome da aldeia."),
        POVO("POVO", true, "Nome do povo indígena.", "POVOS"),
        SITUACAO("SITUAÇÃO", false, "FUNCIONANDO, PARALISADA ou DESATIVADA."),
        TEM_PREDIO("TEM PRÉDIO", false, "SIM ou NÃO.", "PRÉDIO"),
        ANO_CONSTRUCAO("ANO CONSTRUÇÃO", false, "Ano da construção do prédio.", "ANO DE CONSTRUÇÃO"),
        NUM_SALAS("Nº DE SALAS", false, "Quantidade de salas.", "NÚMERO DE SALAS", "SALAS"),
        TOTAL_ALUNOS("Nº DE ALUNOS", false, "Total de alunos do ano.",
                "NÚMERO DE ALUNOS", "TOTAL DE ALUNOS", "ALUNOS"),
        LATITUDE("LATITUDE", false, "Em graus decimais (-9.97) ou graus, minutos e segundos (9°58'29\"S)."),
        LONGITUDE("LONGITUDE", false, "Em graus decimais (-67.81) ou graus, minutos e segundos (67°48'36\"O)."),
        COORDENADAS("COORDENADAS", false,
                "Formato antigo: latitude nesta coluna e longitude na seguinte. Prefira LATITUDE e LONGITUDE.",
                "COORDENADA"),
        ACESSO("ACESSO", false, "TERRESTRE, FLUVIAL ou MISTO.", "TIPO DE ACESSO"),
        INTERNET("INTERNET", false, "SIM ou NÃO.", "TEM INTERNET"),
        AGUA("ÁGUA", false,
                "REDE PÚBLICA, POÇO, CACIMBA, RIO, IGARAPÉ, VERTENTE, RIO/VERTENTE, OUTRO ou NÃO.",
                "TIPO DE ÁGUA"),
        ENERGIA("ENERGIA", false,
                "REDE PÚBLICA (ou REDE ELÉTRICA), GERADOR, SOLAR, OUTRO ou NÃO.",
                "TIPO DE ENERGIA"),
        PROFESSOR("TEM PROFESSOR RESPONSÁVEL", false, "SIM ou NÃO.", "PROFESSOR RESPONSÁVEL"),
        EQUIPE_GESTORA("TEM EQUIPE GESTORA", false, "SIM ou NÃO.", "EQUIPE GESTORA"),
        MERENDEIRA("TEM MERENDEIRA", false, "SIM ou NÃO.", "MERENDEIRA"),
        ZELADORA("TEM ZELADORA", false, "SIM ou NÃO.", "ZELADORA"),
        BARQUEIRO("TEM BARQUEIRO", false, "SIM ou NÃO.", "BARQUEIRO"),
        TRANSPORTE("TEM NECESSIDADE DE TRANSPORTE ESCOLAR", false, "SIM ou NÃO.",
                "NECESSIDADE DE TRANSPORTE ESCOLAR", "TRANSPORTE ESCOLAR");

        private final String titulo;
        private final boolean obrigatoria;
        private final String descricao;
        private final List<String> sinonimos;

        Coluna(String titulo, boolean obrigatoria, String descricao, String... sinonimos) {
            this.titulo = titulo;
            this.obrigatoria = obrigatoria;
            this.descricao = descricao;
            this.sinonimos = List.of(sinonimos);
        }

        @Override public String titulo() { return titulo; }
        @Override public boolean obrigatoria() { return obrigatoria; }
        @Override public String descricao() { return descricao; }
        @Override public List<String> sinonimos() { return sinonimos; }
    }

    public static final LayoutPlanilha<Coluna> LAYOUT = new LayoutPlanilha<>(
            Coluna.class,
            "Escolas e infraestrutura",
            "de escolas e infraestrutura",
            List.of(
                    "Cada linha é uma escola. Os dados são gravados no ano escolhido na importação; o ano é criado se ainda não existir.",
                    "As colunas podem estar em qualquer ordem e a aba pode ter qualquer nome. Colunas extras são ignoradas.",
                    "Município, terra indígena, povo e aldeia ainda não cadastrados são criados.",
                    "Colunas opcionais ausentes não alteram os dados já gravados; células vazias gravam o campo como não informado.",
                    "Valores fora das opções de situação, acesso, água e energia são gravados como não informados."
            )
    );

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    PovoRepository povoRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    InfraestruturaEscolaRepository infraestruturaRepository;

    @Autowired
    EquipeEscolaRepository equipeEscolaRepository;

    /**
     * @param ano ano de referência dos dados; nulo usa o ano mais recente cadastrado
     */
    public ImportacaoResultadoDTO importarInfraestrutura(
            MultipartFile arquivo,
            Integer ano
    ) {
        validarArquivo(arquivo);

        ImportacaoContador contador =
                new ImportacaoContador();

        try (
                InputStream inputStream =
                        arquivo.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {
            PlanilhaLocalizada<Coluna> planilha =
                    localizarPlanilha(workbook);

            List<Row> linhas = planilha.linhas();
            linhas.forEach(linha -> contador.incrementarLinhasLidas());

            AnoReferencia anoReferencia =
                    resolverAno(ano);

            Map<String, Municipio> municipiosPorNome =
                    carregarMunicipiosPorNome();

            for (Row linha : linhas) {
                if (!isAnexo(planilha, linha)) {
                    processarLinha(
                            new Linha(planilha, linha),
                            anoReferencia,
                            municipiosPorNome,
                            contador
                    );
                }
            }

            for (Row linha : linhas) {
                if (isAnexo(planilha, linha)) {
                    processarLinha(
                            new Linha(planilha, linha),
                            anoReferencia,
                            municipiosPorNome,
                            contador
                    );
                }
            }

            return contador.gerarResultado();

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Não foi possível ler o arquivo Excel.",
                    e
            );
        }
    }

    /**
     * Sem LATITUDE e LONGITUDE, aceita o formato antigo: COORDENADAS com a latitude
     * e a longitude na coluna seguinte, sem título.
     */
    private PlanilhaLocalizada<Coluna> localizarPlanilha(
            Workbook workbook
    ) {
        PlanilhaLocalizada<Coluna> planilha =
                LAYOUT.localizar(workbook);

        if (planilha.possui(Coluna.COORDENADAS)
                && !planilha.possui(Coluna.LATITUDE)
                && !planilha.possui(Coluna.LONGITUDE)) {

            int posicao = planilha.coluna(Coluna.COORDENADAS);

            planilha = planilha
                    .comColuna(Coluna.LATITUDE, posicao)
                    .comColuna(Coluna.LONGITUDE, posicao + 1);
        }

        return planilha;
    }

    private void processarLinha(
            Linha linha,
            AnoReferencia anoReferencia,
            Map<String, Municipio> municipiosPorNome,
            ImportacaoContador contador
    ) {
        int numeroLinhaExcel =
                linha.row().getRowNum() + 1;

        try {
            String codigo = obrigatorio(
                    linha.codigo(Coluna.CODIGO),
                    "Código da escola"
            );

            String nomeEscola = obrigatorio(
                    linha.texto(Coluna.ESCOLA),
                    "Nome da escola"
            );

            Municipio municipio =
                    buscarMunicipio(
                            linha,
                            municipiosPorNome
                    );

            TerraIndigena terraIndigena =
                    buscarOuCriarTerraIndigena(
                            linha,
                            municipio,
                            contador
                    );

            Povo povo = buscarOuCriarPovo(
                    linha,
                    contador
            );

            Aldeia aldeia = buscarOuCriarAldeia(
                    linha,
                    terraIndigena,
                    povo,
                    contador
            );

            Escola escola = buscarOuCriarEscola(
                    linha,
                    codigo,
                    nomeEscola,
                    aldeia,
                    contador
            );

            RegistroAnual registroAnual =
                    buscarOuCriarRegistroAnual(
                            linha,
                            escola,
                            anoReferencia,
                            contador
                    );

            salvarInfraestrutura(
                    linha,
                    registroAnual,
                    contador
            );

            salvarEquipe(
                    linha,
                    registroAnual,
                    anoReferencia,
                    contador
            );

        } catch (Exception e) {
            contador.adicionarErro(
                    numeroLinhaExcel,
                    obterMensagemErro(e)
            );
        }
    }

    private Municipio buscarMunicipio(
            Linha linha,
            Map<String, Municipio> municipiosPorNome
    ) {
        String nome = obrigatorio(
                linha.texto(Coluna.MUNICIPIO),
                "Município"
        );

        Municipio municipio = municipiosPorNome.get(
                ExcelUtils.normalizar(nome)
        );

        if (municipio != null) {
            return municipio;
        }

        throw new IllegalArgumentException(
                "Município não cadastrado: " + nome
        );
    }

    private Map<String, Municipio> carregarMunicipiosPorNome() {
        Map<String, Municipio> municipiosPorNome = new HashMap<>();

        for (Municipio municipio : municipioRepository.findAll()) {
            municipiosPorNome.put(
                    ExcelUtils.normalizar(municipio.getNome()),
                    municipio
            );
        }

        adicionarAlias(
                municipiosPorNome,
                "M.THAUMATURGO",
                "Marechal Thaumaturgo"
        );
        adicionarAlias(
                municipiosPorNome,
                "S.R. DO PURUS",
                "Santa Rosa do Purus"
        );

        return municipiosPorNome;
    }

    private void adicionarAlias(
            Map<String, Municipio> municipiosPorNome,
            String alias,
            String nomeOficial
    ) {
        Municipio municipio = municipiosPorNome.get(
                ExcelUtils.normalizar(nomeOficial)
        );

        if (municipio == null) {
            throw new IllegalStateException(
                    "Município obrigatório não cadastrado: " + nomeOficial
            );
        }

        municipiosPorNome.put(
                ExcelUtils.normalizar(alias),
                municipio
        );
    }

    private TerraIndigena buscarOuCriarTerraIndigena(
            Linha linha,
            Municipio municipio,
            ImportacaoContador contador
    ) {
        String nome = obrigatorio(
                linha.texto(Coluna.TERRA_INDIGENA),
                "Terra indígena"
        );

        TerraIndigena terraIndigena =
                terraIndigenaRepository
                        .findByNomeIgnoreCaseAndMunicipio(
                                nome,
                                municipio
                        )
                        .orElse(null);

        if (terraIndigena == null) {
            terraIndigena = new TerraIndigena();

            terraIndigena.setNome(nome);
            terraIndigena.setMunicipio(municipio);

            terraIndigena =
                    terraIndigenaRepository.save(
                            terraIndigena
                    );

            contador.incrementarTerrasIndigenasCriadas();
        }

        return terraIndigena;
    }

    private Povo buscarOuCriarPovo(
            Linha linha,
            ImportacaoContador contador
    ) {
        String nome = obrigatorio(
                linha.texto(Coluna.POVO),
                "Povo"
        );

        Povo povo = povoRepository
                .findByNomeIgnoreCase(nome)
                .orElse(null);

        if (povo == null) {
            povo = new Povo();
            povo.setNome(nome);

            povo = povoRepository.save(povo);

            contador.incrementarPovosCriados();
        }

        return povo;
    }

    private Aldeia buscarOuCriarAldeia(
            Linha linha,
            TerraIndigena terraIndigena,
            Povo povo,
            ImportacaoContador contador
    ) {
        String nome = obrigatorio(
                linha.texto(Coluna.ALDEIA),
                "Aldeia"
        );

        Aldeia aldeia = aldeiaRepository
                .findByNomeIgnoreCaseAndTerraIndigenaAndPovo(
                        nome,
                        terraIndigena,
                        povo
                )
                .orElse(null);

        if (aldeia == null) {
            aldeia = new Aldeia();

            aldeia.setNome(nome);
            aldeia.setTerraIndigena(terraIndigena);
            aldeia.setPovo(povo);

            aldeia = aldeiaRepository.save(aldeia);

            contador.incrementarAldeiasCriadas();
        }

        return aldeia;
    }

    private Escola buscarOuCriarEscola(
            Linha linha,
            String codigo,
            String nome,
            Aldeia aldeia,
            ImportacaoContador contador
    ) {
        String tipoUnidade =
                isAnexo(linha.planilha(), linha.row()) ? "ANEXO" : "SEDE";

        Escola escola = escolaRepository
                .findFirstByCodigoAndAldeia_NomeIgnoreCase(
                        codigo,
                        aldeia.getNome()
                )
                .orElse(null);

        boolean novaEscola = escola == null;

        if (novaEscola) {
            escola = new Escola();
            escola.setCodigo(codigo);
            escola.setNome(nome);
        }

        escola.setTipoUnidade(tipoUnidade);
        escola.setAldeia(aldeia);

        if ("ANEXO".equals(tipoUnidade)) {
            Escola escolaSede = escolaRepository
                    .findFirstByCodigoAndTipoUnidadeIgnoreCase(
                            codigo,
                            "SEDE"
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Escola-sede não encontrada " +
                                            "para o anexo. Código: " +
                                            codigo
                            )
                    );

            escola.setEscolaSede(escolaSede);

        } else {
            escola.setEscolaSede(null);
        }

        escola = escolaRepository.save(escola);

        if (novaEscola) {
            contador.incrementarEscolasCriadas();
        } else {
            contador.incrementarRegistrosAtualizados();
        }

        return escola;
    }

    private RegistroAnual buscarOuCriarRegistroAnual(
            Linha linha,
            Escola escola,
            AnoReferencia anoReferencia,
            ImportacaoContador contador
    ) {
        RegistroAnual registroAnual =
                registroAnualRepository
                        .findByEscolaAndAnoReferencia(
                                escola,
                                anoReferencia
                        )
                        .orElse(null);

        boolean novoRegistro =
                registroAnual == null;

        if (novoRegistro) {
            registroAnual = new RegistroAnual();
            registroAnual.setEscola(escola);
            registroAnual.setAnoReferencia(
                    anoReferencia
            );
        }

        registroAnual.setRede("ESTADUAL");
        registroAnual.setLocalFuncionamento("RURAL");

        linha.seExistir(
                Coluna.TOTAL_ALUNOS,
                linha::inteiro,
                registroAnual::setTotalAlunos
        );

        registroAnual =
                registroAnualRepository.save(
                        registroAnual
                );

        if (novoRegistro) {
            contador.incrementarRegistrosAnuaisCriados();
        } else {
            contador.incrementarRegistrosAtualizados();
        }

        return registroAnual;
    }

    private void salvarInfraestrutura(
            Linha linha,
            RegistroAnual registroAnual,
            ImportacaoContador contador
    ) {
        InfraestruturaEscola infraestrutura =
                infraestruturaRepository
                        .findByRegistroAnual_Id(
                                registroAnual.getId()
                        )
                        .orElse(null);

        boolean novaInfraestrutura =
                infraestrutura == null;

        if (novaInfraestrutura) {
            infraestrutura =
                    new InfraestruturaEscola();

            infraestrutura.setRegistroAnual(
                    registroAnual
            );
        }

        linha.seExistir(Coluna.ANO_CONSTRUCAO, linha::ano, infraestrutura::setAnoConstrucao);
        linha.seExistir(Coluna.NUM_SALAS, linha::inteiro, infraestrutura::setNumSalas);

        linha.seExistir(
                Coluna.LATITUDE,
                coluna -> CoordenadaUtils.converterLatitude(linha.texto(coluna)),
                infraestrutura::setLatitude
        );

        linha.seExistir(
                Coluna.LONGITUDE,
                coluna -> CoordenadaUtils.converterLongitude(linha.texto(coluna)),
                infraestrutura::setLongitude
        );

        // Valores fora das opções do sistema (ex.: "DESATIVADA" na coluna de
        // acesso das escolas paralisadas) são gravados como não informados
        linha.seExistir(
                Coluna.SITUACAO,
                coluna -> OpcoesInfraestrutura.situacaoImportada(linha.texto(coluna)),
                infraestrutura::setSituacao
        );

        linha.seExistir(
                Coluna.ACESSO,
                coluna -> OpcoesInfraestrutura.tipoAcessoImportado(linha.texto(coluna)),
                infraestrutura::setTipoAcesso
        );

        linha.seExistir(Coluna.TEM_PREDIO, linha::booleano, infraestrutura::setTemPredio);
        linha.seExistir(Coluna.INTERNET, linha::booleano, infraestrutura::setTemInternet);

        linha.seExistir(
                Coluna.AGUA,
                coluna -> OpcoesInfraestrutura.tipoAguaImportado(linha.texto(coluna)),
                infraestrutura::setTipoAgua
        );

        linha.seExistir(
                Coluna.ENERGIA,
                coluna -> OpcoesInfraestrutura.tipoEnergiaImportado(linha.texto(coluna)),
                infraestrutura::setTipoEnergia
        );

        // A coluna RELATÓRIO FOTOGRÁFICO da planilha não é lida: temRegistroFotografico indica uma
        // foto anexada no sistema, e marcá-lo sem foto travava a edição da escola no formulário

        infraestruturaRepository.save(infraestrutura);

        if (novaInfraestrutura) {
            contador.incrementarInfraestruturasCriadas();
        } else {
            contador.incrementarRegistrosAtualizados();
        }
    }

    private void salvarEquipe(
            Linha linha,
            RegistroAnual registroAnual,
            AnoReferencia anoReferencia,
            ImportacaoContador contador
    ) {
        EquipeEscola equipe = equipeEscolaRepository
                .findByRegistroAnual_Id(
                        registroAnual.getId()
                )
                .orElse(null);

        boolean novaEquipe = equipe == null;

        if (novaEquipe) {
            equipe = new EquipeEscola();
            equipe.setRegistroAnual(registroAnual);        }

        equipe.setAnoReferencia(anoReferencia);

        linha.seExistir(Coluna.PROFESSOR, linha::sim, equipe::setTemProfessorResponsavel);
        linha.seExistir(Coluna.EQUIPE_GESTORA, linha::sim, equipe::setTemEquipeGestora);
        linha.seExistir(Coluna.MERENDEIRA, linha::sim, equipe::setTemMerendeira);
        linha.seExistir(Coluna.ZELADORA, linha::sim, equipe::setTemZeladora);
        linha.seExistir(Coluna.BARQUEIRO, linha::sim, equipe::setTemBarqueiro);
        linha.seExistir(Coluna.TRANSPORTE, linha::sim, equipe::setNecessitaTransporte);

        equipeEscolaRepository.save(equipe);

        if (novaEquipe) {
            contador.incrementarEquipesCriadas();
        } else {
            contador.incrementarRegistrosAtualizados();
        }
    }

    /**
     * O ano informado é criado se ainda não existir, dentro de um intervalo
     * plausível; sem ano, usa o mais recente cadastrado.
     */
    private AnoReferencia resolverAno(Integer ano) {
        if (ano == null) {
            return anoReferenciaRepository
                    .findTopByOrderByAnoDesc()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Informe o ano de referência da planilha."
                    ));
        }

        int anoMaximo = Year.now(FUSO).getValue() + 1;

        if (ano < ANO_MINIMO || ano > anoMaximo) {
            throw new IllegalArgumentException(
                    "Ano de referência inválido: " + ano +
                            ". Informe um ano entre " + ANO_MINIMO +
                            " e " + anoMaximo + "."
            );
        }

        return buscarOuCriarAno(ano);
    }

    private AnoReferencia buscarOuCriarAno(
            Integer ano
    ) {
        AnoReferencia anoReferencia =
                anoReferenciaRepository
                        .findByAno(ano)
                        .orElse(null);

        if (anoReferencia == null) {
            anoReferencia = new AnoReferencia();
            anoReferencia.setAno(ano);

            anoReferencia =
                    anoReferenciaRepository.save(
                            anoReferencia
                    );
        }

        return anoReferencia;
    }

    private static boolean isAnexo(
            PlanilhaLocalizada<Coluna> planilha,
            Row linha
    ) {
        return "ANEXO".equals(ExcelUtils.normalizar(
                ExcelUtils.lerTexto(linha, planilha.coluna(Coluna.TIPO_UNIDADE))
        )) || "ANEXO".equals(ExcelUtils.normalizar(
                ExcelUtils.lerTexto(linha, planilha.coluna(Coluna.ORDEM))
        ));
    }

    private String obrigatorio(
            String valor,
            String nomeCampo
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    nomeCampo + " é obrigatório."
            );
        }

        return valor;
    }

    /**
     * Mensagem do erro da linha para o resultado, que aparece na tela. Erros de
     * banco trazem SQL e nomes de constraints, por isso não são repassados.
     */
    private String obterMensagemErro(Exception exception) {
        if (exception instanceof DataAccessException) {
            log.warn("Falha ao gravar uma linha da planilha de escolas", exception);
            return "Não foi possível gravar a linha porque ela conflita com dados já cadastrados.";
        }

        if (exception.getMessage() == null ||
                exception.getMessage().isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return exception.getMessage();
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selecione um arquivo Excel."
            );
        }

        String nomeArquivo =
                arquivo.getOriginalFilename();

        if (nomeArquivo == null ||
                !nomeArquivo.toLowerCase().endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "O arquivo deve estar no formato .xlsx."
            );
        }
    }

    /** Uma linha de dados com as posições das colunas encontradas no cabeçalho. */
    private record Linha(PlanilhaLocalizada<Coluna> planilha, Row row) {

        String texto(Coluna coluna) {
            return ExcelUtils.lerTexto(row, planilha.coluna(coluna));
        }

        String codigo(Coluna coluna) {
            return ExcelUtils.lerCodigo(row, planilha.coluna(coluna));
        }

        Integer inteiro(Coluna coluna) {
            return ExcelUtils.lerInteiro(row, planilha.coluna(coluna));
        }

        Integer ano(Coluna coluna) {
            return ExcelUtils.lerAno(row, planilha.coluna(coluna));
        }

        Boolean booleano(Coluna coluna) {
            return ExcelUtils.lerBooleano(row, planilha.coluna(coluna));
        }

        /** Célula vazia conta como "não", como nos indicadores da equipe. */
        Boolean sim(Coluna coluna) {
            return Boolean.TRUE.equals(booleano(coluna));
        }

        /** Atribui o valor lido só quando a coluna existe na planilha. */
        <T> void seExistir(Coluna coluna, Function<Coluna, T> ler, Consumer<T> atribuir) {
            if (planilha.possui(coluna)) {
                atribuir.accept(ler.apply(coluna));
            }
        }
    }
}
