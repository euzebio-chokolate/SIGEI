package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.MatriculaNivelRepository;
import ac.gov.br.sigei_backend.repository.MatriculaSerieRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import ac.gov.br.sigei_backend.utils.ImportacaoMatriculasContador;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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

@Service
public class ImportacaoMatriculasService {

    private static final int ANO_REFERENCIA = 2025;

    private static final int COL_ORDEM = 0;
    private static final int COL_CODIGO = 2;
    private static final int COL_NOME_ESCOLA = 3;
    private static final int COL_ALDEIA = 6;
    private static final int COL_MASCULINO = 7;
    private static final int COL_FEMININO = 8;
    private static final int COL_INFANTIL = 9;

    private static final int COL_PRIMEIRO_ANO = 10;
    private static final int COL_SEGUNDO_ANO = 11;
    private static final int COL_TERCEIRO_ANO = 12;
    private static final int COL_QUARTO_ANO = 13;
    private static final int COL_QUINTO_ANO = 14;

    private static final int COL_SEXTO_ANO = 16;
    private static final int COL_SETIMO_ANO = 17;
    private static final int COL_OITAVO_ANO = 18;
    private static final int COL_NONO_ANO = 19;

    private static final int COL_PRIMEIRA_SERIE = 21;
    private static final int COL_SEGUNDA_SERIE = 22;
    private static final int COL_TERCEIRA_SERIE = 23;

    private static final int COL_EJA = 25;
    private static final int COL_AEE = 26;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    MatriculaNivelRepository matriculaNivelRepository;

    @Autowired
    MatriculaSerieRepository matriculaSerieRepository;

    @Transactional
    public ImportacaoMatriculasResultadoDTO importar(
            MultipartFile arquivo
    ) {
        validarArquivo(arquivo);

        ImportacaoMatriculasContador contador =
                new ImportacaoMatriculasContador();

        try (
                InputStream inputStream =
                        arquivo.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {
            Sheet planilha =
                    workbook.getSheet("Censo_2025");

            if (planilha == null) {
                throw new IllegalArgumentException(
                        "A aba Censo_2025 não foi encontrada."
                );
            }

            /*
             * As linhas 1 e 2 são cabeçalhos.
             * O índice 2 corresponde à linha 3 do Excel.
             */
            for (int indice = 2;
                 indice <= planilha.getLastRowNum();
                 indice++) {

                Row linha = planilha.getRow(indice);

                if (ExcelUtils.linhaVazia(linha)) {
                    continue;
                }

                String codigo = ExcelUtils.lerCodigo(
                        linha,
                        COL_CODIGO
                );

                /*
                 * Ignora a linha de totalização no final.
                 */
                if (codigo == null) {
                    continue;
                }

                contador.incrementarLinhasLidas();

                processarLinha(linha, contador);
            }

            return contador.gerarResultado();

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Não foi possível ler a planilha.",
                    e
            );
        }
    }

    private void processarLinha(
            Row linha,
            ImportacaoMatriculasContador contador
    ) {
        int numeroLinha = linha.getRowNum() + 1;

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
                            COL_NOME_ESCOLA
                    ),
                    "Nome da escola"
            );

            String nomeAldeia = ExcelUtils.lerTexto(
                    linha,
                    COL_ALDEIA
            );

            boolean anexo = isAnexo(linha);

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
                                    ANO_REFERENCIA
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Registro anual de 2025 " +
                                                    "não encontrado para a escola " +
                                                    escola.getNome()
                                    )
                            );

            contador.incrementarRegistrosAnuaisEncontrados();

            salvarMatriculas(
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
            Row linha,
            RegistroAnual registroAnual,
            ImportacaoMatriculasContador contador
    ) {
        int masculino = valor(
                ExcelUtils.lerInteiro(
                        linha,
                        COL_MASCULINO
                )
        );

        int feminino = valor(
                ExcelUtils.lerInteiro(
                        linha,
                        COL_FEMININO
                )
        );

        int infantil = valor(
                ExcelUtils.lerInteiro(
                        linha,
                        COL_INFANTIL
                )
        );

        int primeiroAno = lerValor(
                linha,
                COL_PRIMEIRO_ANO
        );

        int segundoAno = lerValor(
                linha,
                COL_SEGUNDO_ANO
        );

        int terceiroAno = lerValor(
                linha,
                COL_TERCEIRO_ANO
        );

        int quartoAno = lerValor(
                linha,
                COL_QUARTO_ANO
        );

        int quintoAno = lerValor(
                linha,
                COL_QUINTO_ANO
        );

        int sextoAno = lerValor(
                linha,
                COL_SEXTO_ANO
        );

        int setimoAno = lerValor(
                linha,
                COL_SETIMO_ANO
        );

        int oitavoAno = lerValor(
                linha,
                COL_OITAVO_ANO
        );

        int nonoAno = lerValor(
                linha,
                COL_NONO_ANO
        );

        int primeiraSerie = lerValor(
                linha,
                COL_PRIMEIRA_SERIE
        );

        int segundaSerie = lerValor(
                linha,
                COL_SEGUNDA_SERIE
        );

        int terceiraSerie = lerValor(
                linha,
                COL_TERCEIRA_SERIE
        );

        int eja = lerValor(linha, COL_EJA);
        int aee = lerValor(linha, COL_AEE);

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

    private int lerValor(
            Row linha,
            int coluna
    ) {
        return valor(
                ExcelUtils.lerInteiro(
                        linha,
                        coluna
                )
        );
    }

    private int valor(Integer valor) {
        return valor == null ? 0 : valor;
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