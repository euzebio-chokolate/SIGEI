package ac.gov.br.sigei_backend.service.importacao;

import ac.gov.br.sigei_backend.dto.response.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.utils.CoordenadaUtils;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import ac.gov.br.sigei_backend.utils.ImportacaoContador;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportacaoExcelService {

    private static final int ANO_PLANILHA = 2025;

    private static final int COL_ORDEM = 0;
    private static final int COL_MUNICIPIO = 1;
    private static final int COL_CODIGO = 2;
    private static final int COL_ESCOLA = 3;
    private static final int COL_TERRA_INDIGENA = 4;
    private static final int COL_ALDEIA = 5;
    private static final int COL_POVO = 6;
    private static final int COL_SITUACAO = 7;
    private static final int COL_TEM_PREDIO = 8;
    private static final int COL_ANO_CONSTRUCAO = 9;
    private static final int COL_NUM_SALAS = 10;
    private static final int COL_TOTAL_ALUNOS = 11;
    private static final int COL_LATITUDE = 12;
    private static final int COL_LONGITUDE = 13;
    private static final int COL_ACESSO = 14;
    private static final int COL_INTERNET = 15;
    private static final int COL_AGUA = 16;
    private static final int COL_ENERGIA = 17;
    private static final int COL_FOTOGRAFICO = 18;
    private static final int COL_PROFESSOR = 19;
    private static final int COL_EQUIPE_GESTORA = 20;
    private static final int COL_MERENDEIRA = 21;
    private static final int COL_ZELADORA = 22;
    private static final int COL_BARQUEIRO = 23;
    private static final int COL_TRANSPORTE = 24;

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

    public ImportacaoResultadoDTO importarInfraestrutura(
            MultipartFile arquivo
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
            Sheet planilha = workbook.getSheet("Planilha4");

            if (planilha == null) {
                throw new IllegalArgumentException(
                        "A aba Planilha4 não foi encontrada."
                );
            }

            List<Row> linhas = carregarLinhas(
                    planilha,
                    contador
            );

            AnoReferencia anoReferencia =
                    buscarOuCriarAno(ANO_PLANILHA);

            Map<String, Municipio> municipiosPorNome =
                    carregarMunicipiosPorNome();

            for (Row linha : linhas) {
                if (!isAnexo(linha)) {
                    processarLinha(
                            linha,
                            anoReferencia,
                            municipiosPorNome,
                            contador
                    );
                }
            }

            for (Row linha : linhas) {
                if (isAnexo(linha)) {
                    processarLinha(
                            linha,
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

    private List<Row> carregarLinhas(
            Sheet planilha,
            ImportacaoContador contador
    ) {
        List<Row> linhas = new ArrayList<>();

        for (int indice = 1;
             indice <= planilha.getLastRowNum();
             indice++) {

            Row linha = planilha.getRow(indice);

            if (ExcelUtils.linhaVazia(linha)) {
                continue;
            }

            linhas.add(linha);
            contador.incrementarLinhasLidas();
        }

        return linhas;
    }

    private void processarLinha(
            Row linha,
            AnoReferencia anoReferencia,
            Map<String, Municipio> municipiosPorNome,
            ImportacaoContador contador
    ) {
        int numeroLinhaExcel =
                linha.getRowNum() + 1;

        try {
            String codigo = obrigatorio(
                    ExcelUtils.lerCodigo(
                            linha,
                            COL_CODIGO
                    ),
                    "Código da escola"
            );

            String nomeEscola = obrigatorio(
                    ExcelUtils.lerTexto(
                            linha,
                            COL_ESCOLA
                    ),
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
            Row linha,
            Map<String, Municipio> municipiosPorNome
    ) {
        String nome = obrigatorio(
                ExcelUtils.lerTexto(
                        linha,
                        COL_MUNICIPIO
                ),
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
            Row linha,
            Municipio municipio,
            ImportacaoContador contador
    ) {
        String nome = obrigatorio(
                ExcelUtils.lerTexto(
                        linha,
                        COL_TERRA_INDIGENA
                ),
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
            Row linha,
            ImportacaoContador contador
    ) {
        String nome = obrigatorio(
                ExcelUtils.lerTexto(
                        linha,
                        COL_POVO
                ),
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
            Row linha,
            TerraIndigena terraIndigena,
            Povo povo,
            ImportacaoContador contador
    ) {
        String nome = obrigatorio(
                ExcelUtils.lerTexto(
                        linha,
                        COL_ALDEIA
                ),
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
            Row linha,
            String codigo,
            String nome,
            Aldeia aldeia,
            ImportacaoContador contador
    ) {
        String tipoUnidade =
                isAnexo(linha) ? "ANEXO" : "SEDE";

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
            Row linha,
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

        registroAnual.setTotalAlunos(
                ExcelUtils.lerInteiro(
                        linha,
                        COL_TOTAL_ALUNOS
                )
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
            Row linha,
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

        infraestrutura.setAnoConstrucao(
                ExcelUtils.lerInteiro(
                        linha,
                        COL_ANO_CONSTRUCAO
                )
        );

        infraestrutura.setNumSalas(
                ExcelUtils.lerInteiro(
                        linha,
                        COL_NUM_SALAS
                )
        );

        infraestrutura.setLatitude(
                CoordenadaUtils.converterLatitude(
                        ExcelUtils.lerTexto(
                                linha,
                                COL_LATITUDE
                        )
                )
        );

        infraestrutura.setLongitude(
                CoordenadaUtils.converterLongitude(
                        ExcelUtils.lerTexto(
                                linha,
                                COL_LONGITUDE
                        )
                )
        );

        infraestrutura.setSituacao(
                ExcelUtils.lerTexto(
                        linha,
                        COL_SITUACAO
                )
        );

        infraestrutura.setTipoAcesso(
                ExcelUtils.lerTexto(
                        linha,
                        COL_ACESSO
                )
        );

        infraestrutura.setTemPredio(
                ExcelUtils.lerBooleano(
                        linha,
                        COL_TEM_PREDIO
                )
        );

        infraestrutura.setTemInternet(
                ExcelUtils.lerBooleano(
                        linha,
                        COL_INTERNET
                )
        );

        infraestrutura.setTipoAgua(
                ExcelUtils.lerTexto(
                        linha,
                        COL_AGUA
                )
        );

        infraestrutura.setTipoEnergia(
                ExcelUtils.lerTexto(
                        linha,
                        COL_ENERGIA
                )
        );

        infraestrutura.setTemRegistroFotografico(
                ExcelUtils.lerBooleano(
                        linha,
                        COL_FOTOGRAFICO
                )
        );

        infraestruturaRepository.save(infraestrutura);

        if (novaInfraestrutura) {
            contador.incrementarInfraestruturasCriadas();
        } else {
            contador.incrementarRegistrosAtualizados();
        }
    }

    private void salvarEquipe(
            Row linha,
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
            equipe.setRegistroAnual(registroAnual);
        }

        equipe.setAnoReferencia(anoReferencia);

        equipe.setTemProfessorResponsavel(
                Boolean.TRUE.equals(
                        ExcelUtils.lerBooleano(
                                linha,
                                COL_PROFESSOR
                        )
                )
        );

        equipe.setTemEquipeGestora(
                Boolean.TRUE.equals(
                        ExcelUtils.lerBooleano(
                                linha,
                                COL_EQUIPE_GESTORA
                        )
                )
        );

        equipe.setTemMerendeira(
                Boolean.TRUE.equals(
                        ExcelUtils.lerBooleano(
                                linha,
                                COL_MERENDEIRA
                        )
                )
        );

        equipe.setTemZeladora(
                Boolean.TRUE.equals(
                        ExcelUtils.lerBooleano(
                                linha,
                                COL_ZELADORA
                        )
                )
        );

        equipe.setTemBarqueiro(
                Boolean.TRUE.equals(
                        ExcelUtils.lerBooleano(
                                linha,
                                COL_BARQUEIRO
                        )
                )
        );

        equipe.setNecessitaTransporte(
                Boolean.TRUE.equals(
                        ExcelUtils.lerBooleano(
                                linha,
                                COL_TRANSPORTE
                        )
                )
        );

        equipeEscolaRepository.save(equipe);

        if (novaEquipe) {
            contador.incrementarEquipesCriadas();
        } else {
            contador.incrementarRegistrosAtualizados();
        }
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

    private boolean isAnexo(Row linha) {
        String ordem = ExcelUtils.lerTexto(
                linha,
                COL_ORDEM
        );

        return ordem != null &&
                "ANEXO".equals(
                        ExcelUtils.normalizar(ordem)
                );
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

    private String obterMensagemErro(Exception exception) {
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
}
