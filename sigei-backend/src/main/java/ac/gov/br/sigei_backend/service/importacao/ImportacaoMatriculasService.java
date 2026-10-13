package ac.gov.br.sigei_backend.service.importacao;

import ac.gov.br.sigei_backend.dto.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.MatriculaNivelRepository;
import ac.gov.br.sigei_backend.repository.MatriculaSerieRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.utils.ColunaPlanilha;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import ac.gov.br.sigei_backend.utils.ImportacaoMatriculasContador;
import ac.gov.br.sigei_backend.utils.LayoutPlanilha;
import ac.gov.br.sigei_backend.utils.LayoutPlanilha.PlanilhaLocalizada;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Importa as matrículas por etapa das escolas já cadastradas em um ano. As colunas
 * são localizadas pelo cabeçalho ({@link Coluna}), que pode ocupar duas linhas.
 */
@Service
public class ImportacaoMatriculasService {

    public enum Coluna implements ColunaPlanilha {
        ORDEM("ORD.", false,
                "Número de ordem. O valor ANEXO indica um anexo quando não há a coluna TIPO DE UNIDADE.",
                "ORDEM"),
        TIPO_UNIDADE("TIPO DE UNIDADE", false, "SEDE ou ANEXO.", "TIPO UNIDADE"),
        CODIGO("CÓDIGO", true, "Código INEP da escola. Linhas sem código, como a de totais, são ignoradas.",
                "CÓDIGO INEP", "INEP"),
        NOME_ESCOLA("NOME DA ESCOLA", true, "Nome da escola.", "ESCOLA"),
        ALDEIA("ALDEIA", false, "Nome da aldeia; distingue unidades com o mesmo código."),
        MASCULINO("M", true, "Total de alunos do sexo masculino.", "MASCULINO"),
        FEMININO("F", true, "Total de alunos do sexo feminino.", "FEMININO"),
        INFANTIL("ENSINO INFANTIL", true, "Matrículas na educação infantil.", "EDUCAÇÃO INFANTIL", "INFANTIL"),
        PRIMEIRO_ANO("1º ANO", true, "Matrículas no 1º ano do ensino fundamental."),
        SEGUNDO_ANO("2º ANO", true, "Matrículas no 2º ano do ensino fundamental."),
        TERCEIRO_ANO("3º ANO", true, "Matrículas no 3º ano do ensino fundamental."),
        QUARTO_ANO("4º ANO", true, "Matrículas no 4º ano do ensino fundamental."),
        QUINTO_ANO("5º ANO", true, "Matrículas no 5º ano do ensino fundamental."),
        SEXTO_ANO("6º ANO", true, "Matrículas no 6º ano do ensino fundamental."),
        SETIMO_ANO("7º ANO", true, "Matrículas no 7º ano do ensino fundamental."),
        OITAVO_ANO("8º ANO", true, "Matrículas no 8º ano do ensino fundamental."),
        NONO_ANO("9º ANO", true, "Matrículas no 9º ano do ensino fundamental."),
        PRIMEIRA_SERIE("1ª SÉRIE", true, "Matrículas na 1ª série do ensino médio."),
        SEGUNDA_SERIE("2ª SÉRIE", true, "Matrículas na 2ª série do ensino médio."),
        TERCEIRA_SERIE("3ª SÉRIE", true, "Matrículas na 3ª série do ensino médio."),
        EJA("EJA", false, "Matrículas na educação de jovens e adultos. Sem a coluna, zero."),
        AEE("AEE", false, "Matrículas no atendimento educacional especializado. Sem a coluna, zero.");

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
            "Matrículas",
            "de matrículas",
            List.of(
                    "Cada linha traz as matrículas de uma escola já cadastrada no ano escolhido na importação.",
                    "As colunas podem estar em qualquer ordem e a aba pode ter qualquer nome. Colunas extras, como subtotais, são ignoradas.",
                    "O cabeçalho pode ocupar duas linhas, com as etapas agrupadas sob um título mesclado (ex.: Matrículas).",
                    "M + F deve ser igual à soma das etapas; senão, a linha não é importada.",
                    "As matrículas importadas substituem as do registro anual da escola, e o total de alunos é atualizado."
            )
    );

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    MatriculaNivelRepository matriculaNivelRepository;

    @Autowired
    MatriculaSerieRepository matriculaSerieRepository;

    /**
     * @param ano ano dos registros anuais que recebem as matrículas; nulo usa o
     *            ano mais recente cadastrado
     */
    @Transactional
    public ImportacaoMatriculasResultadoDTO importar(
            MultipartFile arquivo,
            Integer ano
    ) {
        validarArquivo(arquivo);

        int anoReferencia = resolverAno(ano);

        ImportacaoMatriculasContador contador =
                new ImportacaoMatriculasContador();

        try (
                InputStream inputStream =
                        arquivo.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {
            PlanilhaLocalizada<Coluna> planilha =
                    LAYOUT.localizar(workbook);

            for (Row linha : planilha.linhas()) {
                String codigo = ExcelUtils.lerCodigo(
                        linha,
                        planilha.coluna(Coluna.CODIGO)
                );

                /*
                 * Ignora a linha de totalização no final.
                 */
                if (codigo == null) {
                    continue;
                }

                contador.incrementarLinhasLidas();

                processarLinha(planilha, linha, anoReferencia, contador);
            }

            return contador.gerarResultado();

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Não foi possível ler a planilha.",
                    e
            );
        }
    }

    private int resolverAno(Integer ano) {
        if (ano == null) {
            return anoReferenciaRepository
                    .findTopByOrderByAnoDesc()
                    .map(AnoReferencia::getAno)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nenhum ano de referência cadastrado. " +
                                    "Importe antes as escolas e a infraestrutura."
                    ));
        }

        if (anoReferenciaRepository.findByAno(ano).isEmpty()) {
            throw new IllegalArgumentException(
                    "O ano " + ano + " não está cadastrado. " +
                            "Inicie o ano ou importe antes as escolas e a infraestrutura dele."
            );
        }

        return ano;
    }

    private void processarLinha(
            PlanilhaLocalizada<Coluna> planilha,
            Row linha,
            int ano,
            ImportacaoMatriculasContador contador
    ) {
        int numeroLinha = linha.getRowNum() + 1;

        try {
            String codigo = obrigatorio(
                    ExcelUtils.lerCodigo(
                            linha,
                            planilha.coluna(Coluna.CODIGO)
                    ),
                    "Código da escola"
            );

            String nomeEscola = obrigatorio(
                    ExcelUtils.lerTexto(
                            linha,
                            planilha.coluna(Coluna.NOME_ESCOLA)
                    ),
                    "Nome da escola"
            );

            String nomeAldeia = ExcelUtils.lerTexto(
                    linha,
                    planilha.coluna(Coluna.ALDEIA)
            );

            boolean anexo = isAnexo(planilha, linha);

            Escola escola = localizarEscola(
                    codigo,
                    nomeEscola,
                    nomeAldeia,
                    anexo
            );

            contador.incrementarEscolasEncontradas();

            RegistroAnual registroAnual =
                    registroAnualRepository
                            .findByEscolaAndAnoReferencia_Ano(
                                    escola,
                                    ano
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Registro anual de " + ano +
                                                    " não encontrado para a escola " +
                                                    escola.getNome()
                                    )
                            );

            contador.incrementarRegistrosAnuaisEncontrados();

            salvarMatriculas(
                    planilha,
                    linha,
                    registroAnual,
                    contador
            );

        } catch (Exception e) {
            contador.adicionarErro(
                    numeroLinha,
                    obterMensagemErro(e)
            );
        }
    }

    private Escola localizarEscola(
            String codigo,
            String nomeEscola,
            String nomeAldeia,
            boolean anexo
    ) {
        /*
         * Primeira tentativa: código e aldeia exatamente
         * como estão cadastrados.
         */
        if (nomeAldeia != null) {
            Escola escolaExata = escolaRepository
                    .findFirstByCodigoAndAldeia_NomeIgnoreCase(
                            codigo,
                            nomeAldeia
                    )
                    .orElse(null);

            if (escolaExata != null) {
                return escolaExata;
            }
        }

        List<Escola> candidatas =
                escolaRepository.findAllByCodigo(codigo);

        if (candidatas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhuma escola encontrada para o código " +
                            codigo
            );
        }

        /*
         * Segunda tentativa: comparação da aldeia
         * removendo acentos e espaços repetidos.
         */
        if (nomeAldeia != null) {
            String aldeiaNormalizada =
                    ExcelUtils.normalizar(nomeAldeia);

            List<Escola> porAldeia = candidatas
                    .stream()
                    .filter(escola ->
                            escola.getAldeia() != null &&
                                    aldeiaNormalizada.equals(
                                            ExcelUtils.normalizar(
                                                    escola
                                                            .getAldeia()
                                                            .getNome()
                                            )
                                    )
                    )
                    .toList();

            if (porAldeia.size() == 1) {
                return porAldeia.getFirst();
            }
        }

        /*
         * Terceira tentativa: filtra pelo tipo da unidade.
         */
        String tipoEsperado =
                anexo ? "ANEXO" : "SEDE";

        List<Escola> porTipo = candidatas
                .stream()
                .filter(escola ->
                        tipoEsperado.equalsIgnoreCase(
                                escola.getTipoUnidade()
                        )
                )
                .toList();

        /*
         * Quarta tentativa: compara o nome normalizado.
         */
        String nomeNormalizado =
                normalizarNomeEscola(nomeEscola);

        List<Escola> porNome = porTipo
                .stream()
                .filter(escola ->
                        nomeNormalizado.equals(
                                normalizarNomeEscola(
                                        escola.getNome()
                                )
                        )
                )
                .toList();

        if (porNome.size() == 1) {
            return porNome.getFirst();
        }

        /*
         * Quinta tentativa: comparação aproximada
         * entre os nomes das aldeias.
         */
        if (nomeAldeia != null) {
            Escola porSemelhanca =
                    localizarPorSemelhancaAldeia(
                            porTipo,
                            nomeAldeia
                    );

            if (porSemelhanca != null) {
                return porSemelhanca;
            }
        }

        /*
         * Se existe somente uma unidade do tipo esperado,
         * ela pode ser usada sem ambiguidade.
         */
        if (porTipo.size() == 1) {
            return porTipo.getFirst();
        }

        if (candidatas.size() == 1) {
            return candidatas.getFirst();
        }

        throw new IllegalArgumentException(
                "Não foi possível identificar a unidade. " +
                        "Código: " +
                        codigo +
                        ", escola: " +
                        nomeEscola +
                        ", aldeia: " +
                        nomeAldeia
        );
    }

    private Escola localizarPorSemelhancaAldeia(
            List<Escola> candidatas,
            String nomeAldeia
    ) {
        Set<String> palavrasOrigem =
                separarPalavras(nomeAldeia);

        if (palavrasOrigem.isEmpty()) {
            return null;
        }

        Escola melhorCandidata = null;
        int melhorPontuacao = 0;
        boolean empate = false;

        for (Escola candidata : candidatas) {
            if (candidata.getAldeia() == null) {
                continue;
            }

            Set<String> palavrasCandidata =
                    separarPalavras(
                            candidata
                                    .getAldeia()
                                    .getNome()
                    );

            int pontuacao = 0;

            for (String palavra : palavrasOrigem) {
                if (palavrasCandidata.contains(palavra)) {
                    pontuacao++;
                }
            }

            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhorCandidata = candidata;
                empate = false;

            } else if (pontuacao > 0 &&
                    pontuacao == melhorPontuacao) {

                empate = true;
            }
        }

        if (melhorPontuacao > 0 && !empate) {
            return melhorCandidata;
        }

        return null;
    }

    private Set<String> separarPalavras(String texto) {
        String normalizado =
                ExcelUtils.normalizar(texto);

        if (normalizado == null ||
                normalizado.isBlank()) {

            return Set.of();
        }

        return Arrays
                .stream(normalizado.split(" "))
                .filter(palavra ->
                        palavra.length() > 2 &&
                                !palavra.equals("DOS") &&
                                !palavra.equals("DAS")
                )
                .collect(Collectors.toSet());
    }

    private String normalizarNomeEscola(
            String nome
    ) {
        String normalizado =
                ExcelUtils.normalizar(nome);

        if (normalizado == null) {
            return "";
        }

        return normalizado
                .replace("ESCOLA", "ESC")
                .replace("INDIGENA", "IND")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void salvarMatriculas(
            PlanilhaLocalizada<Coluna> planilha,
            Row linha,
            RegistroAnual registroAnual,
            ImportacaoMatriculasContador contador
    ) {
        int masculino = lerValor(planilha, linha, Coluna.MASCULINO);
        int feminino = lerValor(planilha, linha, Coluna.FEMININO);
        int infantil = lerValor(planilha, linha, Coluna.INFANTIL);

        int primeiroAno = lerValor(planilha, linha, Coluna.PRIMEIRO_ANO);
        int segundoAno = lerValor(planilha, linha, Coluna.SEGUNDO_ANO);
        int terceiroAno = lerValor(planilha, linha, Coluna.TERCEIRO_ANO);
        int quartoAno = lerValor(planilha, linha, Coluna.QUARTO_ANO);
        int quintoAno = lerValor(planilha, linha, Coluna.QUINTO_ANO);

        int sextoAno = lerValor(planilha, linha, Coluna.SEXTO_ANO);
        int setimoAno = lerValor(planilha, linha, Coluna.SETIMO_ANO);
        int oitavoAno = lerValor(planilha, linha, Coluna.OITAVO_ANO);
        int nonoAno = lerValor(planilha, linha, Coluna.NONO_ANO);

        int primeiraSerie = lerValor(planilha, linha, Coluna.PRIMEIRA_SERIE);
        int segundaSerie = lerValor(planilha, linha, Coluna.SEGUNDA_SERIE);
        int terceiraSerie = lerValor(planilha, linha, Coluna.TERCEIRA_SERIE);

        int eja = lerValor(planilha, linha, Coluna.EJA);
        int aee = lerValor(planilha, linha, Coluna.AEE);

        int anosIniciais =
                primeiroAno +
                        segundoAno +
                        terceiroAno +
                        quartoAno +
                        quintoAno;

        int anosFinais =
                sextoAno +
                        setimoAno +
                        oitavoAno +
                        nonoAno;

        int ensinoMedio =
                primeiraSerie +
                        segundaSerie +
                        terceiraSerie;

        int totalGeral = masculino + feminino;

        int totalCalculado =
                infantil +
                        anosIniciais +
                        anosFinais +
                        ensinoMedio +
                        eja +
                        aee;

        if (totalGeral != totalCalculado) {
            throw new IllegalArgumentException(
                    "O total masculino + feminino (" +
                            totalGeral +
                            ") é diferente da soma das etapas (" +
                            totalCalculado +
                            ")."
            );
        }

        matriculaNivelRepository.deleteByRegistro(
                registroAnual
        );

        matriculaSerieRepository.deleteByRegistro(
                registroAnual
        );

        List<MatriculaNivel> niveis =
                new ArrayList<>();

        niveis.add(criarNivel(
                registroAnual,
                "TOTAL_GERAL",
                masculino,
                feminino,
                totalGeral
        ));

        niveis.add(criarNivel(
                registroAnual,
                "ENSINO_INFANTIL",
                null,
                null,
                infantil
        ));

        niveis.add(criarNivel(
                registroAnual,
                "FUNDAMENTAL_ANOS_INICIAIS",
                null,
                null,
                anosIniciais
        ));

        niveis.add(criarNivel(
                registroAnual,
                "FUNDAMENTAL_ANOS_FINAIS",
                null,
                null,
                anosFinais
        ));

        niveis.add(criarNivel(
                registroAnual,
                "ENSINO_MEDIO",
                null,
                null,
                ensinoMedio
        ));

        niveis.add(criarNivel(
                registroAnual,
                "EJA",
                null,
                null,
                eja
        ));

        niveis.add(criarNivel(
                registroAnual,
                "AEE",
                null,
                null,
                aee
        ));

        List<MatriculaSerie> series =
                new ArrayList<>();

        series.add(criarSerie(
                registroAnual,
                "1º ANO",
                "FUNDAMENTAL_ANOS_INICIAIS",
                primeiroAno
        ));

        series.add(criarSerie(
                registroAnual,
                "2º ANO",
                "FUNDAMENTAL_ANOS_INICIAIS",
                segundoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "3º ANO",
                "FUNDAMENTAL_ANOS_INICIAIS",
                terceiroAno
        ));

        series.add(criarSerie(
                registroAnual,
                "4º ANO",
                "FUNDAMENTAL_ANOS_INICIAIS",
                quartoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "5º ANO",
                "FUNDAMENTAL_ANOS_INICIAIS",
                quintoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "6º ANO",
                "FUNDAMENTAL_ANOS_FINAIS",
                sextoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "7º ANO",
                "FUNDAMENTAL_ANOS_FINAIS",
                setimoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "8º ANO",
                "FUNDAMENTAL_ANOS_FINAIS",
                oitavoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "9º ANO",
                "FUNDAMENTAL_ANOS_FINAIS",
                nonoAno
        ));

        series.add(criarSerie(
                registroAnual,
                "1ª SÉRIE",
                "ENSINO_MEDIO",
                primeiraSerie
        ));

        series.add(criarSerie(
                registroAnual,
                "2ª SÉRIE",
                "ENSINO_MEDIO",
                segundaSerie
        ));

        series.add(criarSerie(
                registroAnual,
                "3ª SÉRIE",
                "ENSINO_MEDIO",
                terceiraSerie
        ));

        matriculaNivelRepository.saveAll(niveis);
        matriculaSerieRepository.saveAll(series);

        registroAnual.setTotalAlunos(totalGeral);
        registroAnualRepository.save(registroAnual);

        contador.adicionarNiveisInseridos(
                niveis.size()
        );

        contador.adicionarSeriesInseridas(
                series.size()
        );

        contador.incrementarRegistrosAnuaisAtualizados();
    }

    private MatriculaNivel criarNivel(
            RegistroAnual registroAnual,
            String nivel,
            Integer masculino,
            Integer feminino,
            Integer total
    ) {
        MatriculaNivel matricula =
                new MatriculaNivel();

        matricula.setRegistro(registroAnual);
        matricula.setNivel(nivel);
        matricula.setQtdMasculino(masculino);
        matricula.setQtdFeminino(feminino);
        matricula.setTotal(total);

        return matricula;
    }

    private MatriculaSerie criarSerie(
            RegistroAnual registroAnual,
            String denominacao,
            String etapaEnsino,
            Integer total
    ) {
        MatriculaSerie matricula =
                new MatriculaSerie();

        matricula.setRegistro(registroAnual);
        matricula.setDenominacao(denominacao);
        matricula.setEtapaEnsino(etapaEnsino);
        matricula.setQtdMasculino(null);
        matricula.setQtdFeminino(null);
        matricula.setTotal(total);

        return matricula;
    }

    /** Célula vazia ou coluna opcional ausente valem zero. */
    private int lerValor(
            PlanilhaLocalizada<Coluna> planilha,
            Row linha,
            Coluna coluna
    ) {
        Integer valor = ExcelUtils.lerInteiro(
                linha,
                planilha.coluna(coluna)
        );

        return valor == null ? 0 : valor;
    }

    private boolean isAnexo(
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
            String campo
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " é obrigatório."
            );
        }

        return valor;
    }

    private String obterMensagemErro(
            Exception exception
    ) {
        if (exception.getMessage() == null ||
                exception.getMessage().isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return exception.getMessage();
    }

    private void validarArquivo(
            MultipartFile arquivo
    ) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selecione o arquivo de matrículas."
            );
        }

        String nome =
                arquivo.getOriginalFilename();

        if (nome == null ||
                !nome.toLowerCase().endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "O arquivo deve estar no formato .xlsx."
            );
        }
    }
}