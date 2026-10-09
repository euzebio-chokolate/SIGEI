package ac.gov.br.sigei_backend.service.importacao;

import ac.gov.br.sigei_backend.config.criptografia.CriptografiaDadosSensiveis;
import ac.gov.br.sigei_backend.dto.response.ImportacaoProfessoresResultadoDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.service.LotacaoProfessorService;
import ac.gov.br.sigei_backend.service.ProfessorService;
import ac.gov.br.sigei_backend.utils.CpfUtils;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import ac.gov.br.sigei_backend.utils.ImportacaoProfessoresContador;
import ac.gov.br.sigei_backend.utils.OpcoesProfessor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Importa o quadro de escolas e professores indígenas de 2026 (aba Planilha1).
 * Cada linha é a lotação de um professor em uma escola; "NECESSIDADE" no nome ou no
 * tipo de contrato indica vaga em aberto, e "NEE SEE" na lotação indica o núcleo
 * de educação do município.
 *
 * <p>A planilha não tem o código INEP: a escola é identificada pelo município, pelo
 * nome (ignorando "ESC."/"IND." e pontuação) e, nos anexos ou nomes repetidos, pela
 * aldeia. Linhas cuja escola não é identificada ficam nos erros do resultado.
 *
 * <p>A importação pode ser repetida: professores são localizados pelo CPF ou pelo
 * nome, e lotações pelo professor, local e segmento. Células vazias nunca apagam
 * valores já gravados, porque CPF, matrícula e número do contrato vêm em branco na
 * planilha e são preenchidos depois pelo sistema.
 */
@Service
public class ImportacaoProfessoresService {

    public static final int ANO_REFERENCIA = 2026;

    private static final String ABA = "Planilha1";

    private static final int COL_MUNICIPIO = 1;
    private static final int COL_LOTACAO = 2;
    private static final int COL_ALDEIA = 5;
    private static final int COL_MATRICULA = 6;
    private static final int COL_CONTRATO = 7;
    private static final int COL_NOME = 8;
    private static final int COL_ETNIA = 9;
    private static final int COL_SEXO = 10;
    private static final int COL_CARGO = 11;
    private static final int COL_FORMACAO = 12;
    private static final int COL_SEGMENTO = 13;
    private static final int COL_CPF = 14;
    private static final int COL_INICIO_CONTRATO = 15;
    private static final int COL_AMPARO_LEGAL = 16;
    private static final int COL_TIPO_CONTRATO = 17;

    private static final String NECESSIDADE = "NECESSIDADE";

    /** Palavras que não distinguem escolas: prefixos e preposições. */
    private static final Set<String> PALAVRAS_IGNORADAS = Set.of(
            "ESC", "ESCOLA", "IND", "INDIGENA", "EST", "ESTADUAL",
            "DE", "DA", "DO", "DAS", "DOS", "E"
    );

    private static final Map<String, String> ABREVIACOES = Map.of(
            "DR", "DOUTOR",
            "PROF", "PROFESSOR"
    );

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    LotacaoProfessorRepository lotacaoRepository;

    @Autowired
    ProfessorService professorService;

    @Autowired
    CriptografiaDadosSensiveis criptografia;

    @Transactional
    public ImportacaoProfessoresResultadoDTO importar(MultipartFile arquivo) {
        validarArquivo(arquivo);

        AnoReferencia anoReferencia = anoReferenciaRepository
                .findByAno(ANO_REFERENCIA)
                .orElseThrow(() -> new IllegalArgumentException(
                        "O ano " + ANO_REFERENCIA + " ainda não foi iniciado. " +
                                "Inicie o ano antes de importar o quadro de professores."
                ));

        ImportacaoProfessoresContador contador = new ImportacaoProfessoresContador();

        try (
                InputStream inputStream = arquivo.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            Sheet planilha = workbook.getSheet(ABA);

            if (planilha == null) {
                throw new IllegalArgumentException("A aba " + ABA + " não foi encontrada.");
            }

            validarCabecalho(planilha.getRow(0));

            Contexto contexto = new Contexto(anoReferencia);

            for (int indice = 1; indice <= planilha.getLastRowNum(); indice++) {
                Row linha = planilha.getRow(indice);

                if (ExcelUtils.linhaVazia(linha) || semConteudo(linha)) {
                    continue;
                }

                contador.incrementarLinhasLidas();
                processarLinha(linha, contexto, contador);
            }

            return contador.gerarResultado();

        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a planilha.", e);
        }
    }

    private void processarLinha(Row linha, Contexto contexto, ImportacaoProfessoresContador contador) {
        int numeroLinha = linha.getRowNum() + 1;

        try {
            // Toda a validação acontece antes da primeira gravação: um erro descarta
            // só esta linha, sem deixar professor ou lotação pela metade
            DadosLinha dados = lerLinha(linha, contexto);

            Professor professor = dados.vaga()
                    ? null
                    : gravarProfessor(dados, contexto, contador);

            gravarLotacao(dados, professor, contexto, contador);

        } catch (IllegalArgumentException e) {
            contador.adicionarErro(numeroLinha, e.getMessage());
        }
    }

    private DadosLinha lerLinha(Row linha, Contexto contexto) {
        String nomeMunicipio = obrigatorio(ExcelUtils.lerTexto(linha, COL_MUNICIPIO), "Município");
        Municipio municipio = contexto.municipiosPorNome.get(ExcelUtils.normalizar(nomeMunicipio));

        if (municipio == null) {
            throw new IllegalArgumentException("Município não cadastrado: " + nomeMunicipio);
        }

        String lotacao = obrigatorio(ExcelUtils.lerTexto(linha, COL_LOTACAO), "Lotação");
        boolean nucleo = ehNucleo(lotacao);

        Escola escola = nucleo
                ? null
                : localizarEscola(
                        contexto.escolasPorMunicipio.getOrDefault(municipio.getId(), List.of()),
                        municipio,
                        lotacao,
                        ExcelUtils.lerTexto(linha, COL_ALDEIA)
                );

        String nome = ExcelUtils.lerTexto(linha, COL_NOME);
        String tipoContrato = ExcelUtils.lerTexto(linha, COL_TIPO_CONTRATO);

        boolean vaga = nome == null
                || NECESSIDADE.equals(ExcelUtils.normalizar(nome))
                || NECESSIDADE.equals(ExcelUtils.normalizar(tipoContrato));

        return new DadosLinha(
                municipio,
                escola,
                vaga,
                vaga ? null : nome,
                CpfUtils.normalizar(lerCpf(linha)),
                ExcelUtils.lerCodigo(linha, COL_MATRICULA),
                ExcelUtils.lerCodigo(linha, COL_CONTRATO),
                ExcelUtils.lerTexto(linha, COL_ETNIA),
                OpcoesProfessor.sexoImportado(ExcelUtils.lerTexto(linha, COL_SEXO)),
                ExcelUtils.lerTexto(linha, COL_FORMACAO),
                ExcelUtils.lerTexto(linha, COL_CARGO),
                ExcelUtils.lerTexto(linha, COL_SEGMENTO),
                ExcelUtils.lerData(linha, COL_INICIO_CONTRATO),
                ExcelUtils.lerTexto(linha, COL_AMPARO_LEGAL),
                OpcoesProfessor.tipoContratoImportado(tipoContrato)
        );
    }

    private Professor gravarProfessor(DadosLinha dados, Contexto contexto, ImportacaoProfessoresContador contador) {
        Professor professor = localizarProfessor(dados, contexto);
        boolean novo = professor == null;

        if (novo) {
            professor = new Professor();
            professor.setNome(dados.nome());
        }

        if (dados.cpf() != null && professor.getCpf() == null) {
            professorService.definirCpf(professor, dados.cpf());
        }

        atualizarSePreenchido(dados.matricula(), professor::setMatricula);
        atualizarSePreenchido(dados.etnia(), professor::setEtnia);
        atualizarSePreenchido(dados.sexo(), professor::setSexo);
        atualizarSePreenchido(dados.formacao(), professor::setFormacao);

        professor = professorRepository.save(professor);

        if (novo) {
            contexto.professoresPorNome
                    .computeIfAbsent(ExcelUtils.normalizar(professor.getNome()), chave -> new ArrayList<>())
                    .add(professor);
            contexto.professoresCriados.add(professor);
            contador.incrementarProfessoresCriados();

        } else if (!contexto.professoresCriados.contains(professor)
                && contexto.professoresAtualizados.add(professor)) {

            contador.incrementarProfessoresAtualizados();
        }

        return professor;
    }

    /**
     * Pelo CPF, quando informado; senão pelo nome. Com CPF, só aceita pelo nome um
     * professor ainda sem CPF. Nomes repetidos são desempatados pela etnia.
     */
    private Professor localizarProfessor(DadosLinha dados, Contexto contexto) {
        if (dados.cpf() != null) {
            Optional<Professor> porCpf = professorRepository.findByCpfHash(criptografia.indice(dados.cpf()));

            if (porCpf.isPresent()) {
                return porCpf.get();
            }
        }

        List<Professor> candidatos = contexto.professoresPorNome
                .getOrDefault(ExcelUtils.normalizar(dados.nome()), List.of())
                .stream()
                .filter(professor -> dados.cpf() == null || professor.getCpf() == null)
                .toList();

        if (candidatos.size() > 1 && dados.etnia() != null) {
            List<Professor> mesmaEtnia = candidatos.stream()
                    .filter(professor -> ExcelUtils.normalizar(dados.etnia())
                            .equals(ExcelUtils.normalizar(professor.getEtnia())))
                    .toList();

            if (!mesmaEtnia.isEmpty()) {
                candidatos = mesmaEtnia;
            }
        }

        if (candidatos.size() > 1) {
            throw new IllegalArgumentException(
                    "Há mais de um professor cadastrado com o nome " + dados.nome() +
                            "; informe o CPF na planilha para identificá-lo."
            );
        }

        return candidatos.isEmpty() ? null : candidatos.getFirst();
    }

    private void gravarLotacao(DadosLinha dados, Professor professor, Contexto contexto,
                               ImportacaoProfessoresContador contador) {

        LotacaoProfessor lotacao = localizarLotacao(dados, professor, contexto);
        boolean nova = lotacao == null;

        if (nova) {
            lotacao = new LotacaoProfessor();
            lotacao.setAnoReferencia(contexto.anoReferencia);
            lotacao.setProfessor(professor);
            lotacao.setEscola(dados.escola());
            lotacao.setMunicipio(dados.municipio());
            lotacao.setSegmento(dados.segmento());
        }

        atualizarSePreenchido(dados.cargo(), lotacao::setCargo);
        atualizarSePreenchido(dados.numeroContrato(), lotacao::setNumeroContrato);
        atualizarSePreenchido(dados.inicioContrato(), lotacao::setInicioContrato);
        atualizarSePreenchido(dados.amparoLegal(), lotacao::setAmparoLegal);
        atualizarSePreenchido(dados.tipoContrato(), lotacao::setTipoContrato);

        lotacao = lotacaoRepository.save(lotacao);
        contexto.lotacoesUsadas.add(lotacao);

        if (nova) {
            contador.incrementarLotacoesCriadas();
        } else {
            contador.incrementarLotacoesAtualizadas();
        }

        if (professor == null) {
            contador.incrementarVagasEmAberto();
        }

        if (dados.escola() == null) {
            contador.incrementarLotacoesNoNucleo();
        }
    }

    /**
     * Lotação do mesmo ano com o mesmo professor (ou vaga), local e segmento, ainda
     * não usada nesta importação. Assim, linhas repetidas viram lotações distintas.
     */
    private LotacaoProfessor localizarLotacao(DadosLinha dados, Professor professor, Contexto contexto) {
        if (professor != null && professor.getId() == null) {
            return null;
        }

        String segmento = ExcelUtils.normalizar(dados.segmento());

        return contexto.lotacoesExistentes
                .stream()
                .filter(lotacao -> !contexto.lotacoesUsadas.contains(lotacao))
                .filter(lotacao -> mesmoId(lotacao.getProfessor(), professor))
                .filter(lotacao -> mesmoId(lotacao.getEscola(), dados.escola()))
                .filter(lotacao -> mesmoId(lotacao.getMunicipio(), dados.municipio()))
                .filter(lotacao -> Objects.equals(ExcelUtils.normalizar(lotacao.getSegmento()), segmento))
                .findFirst()
                .orElse(null);
    }

    /**
     * Identifica a escola dentro do município, entre unidades do mesmo tipo (sede ou
     * anexo), pela primeira etapa que encontrar candidatas:
     * <ol>
     *     <li>mesmo nome completo, com o sufixo de anexo ("ANEXO II");</li>
     *     <li>mesmo nome sem o sufixo de anexo;</li>
     *     <li>mesma aldeia e ao menos uma palavra do nome em comum;</li>
     *     <li>nome mais semelhante: mais da metade das palavras em comum, ou todas as
     *     palavras de um nome contidas no outro.</li>
     * </ol>
     * Palavras com uma letra de diferença ("XINAWEDA"/"XINAVEDA") contam como iguais.
     * Se a etapa encontrar mais de uma escola, a aldeia desempata; persistindo o
     * empate, a linha não é importada, em vez de arriscar a escola errada.
     */
    Escola localizarEscola(List<Escola> escolas, Municipio municipio, String lotacao, String aldeia) {
        NomeEscola procurado = NomeEscola.de(lotacao, false);
        String aldeiaProcurada = aldeia == null ? null : normalizarNome(aldeia);

        List<Candidata> candidatas = escolas.stream()
                .map(escola -> new Candidata(
                        escola,
                        NomeEscola.de(escola.getNome(), "ANEXO".equalsIgnoreCase(escola.getTipoUnidade()))
                ))
                .filter(candidata -> candidata.nome().anexo() == procurado.anexo())
                .toList();

        List<List<Escola>> etapas = List.of(
                filtrar(candidatas, candidata -> candidata.nome().completo().equals(procurado.completo())),
                filtrar(candidatas, candidata -> candidata.nome().base().equals(procurado.base())),
                filtrar(candidatas, candidata -> mesmaAldeia(candidata.escola(), aldeiaProcurada)
                        && palavrasEmComum(candidata.nome(), procurado) > 0),
                maisSemelhantes(candidatas, procurado)
        );

        Escola encontrada = etapas.stream()
                .filter(encontradas -> !encontradas.isEmpty())
                .findFirst()
                .map(encontradas -> desempatar(encontradas, aldeiaProcurada))
                .orElse(null);

        if (encontrada == null) {
            throw new IllegalArgumentException(
                    "Escola não identificada em " + municipio.getNome() + ": " + lotacao +
                            (aldeia == null ? "" : " (aldeia " + aldeia + ")")
            );
        }

        return encontrada;
    }

    private record Candidata(Escola escola, NomeEscola nome) {
    }

    private static List<Escola> filtrar(List<Candidata> candidatas, Predicate<Candidata> condicao) {
        return candidatas.stream()
                .filter(condicao)
                .map(Candidata::escola)
                .toList();
    }

    /** Escolas de maior semelhança (Jaccard), desde que passem de metade ou um nome contenha o outro. */
    private static List<Escola> maisSemelhantes(List<Candidata> candidatas, NomeEscola procurado) {
        List<Escola> melhores = new ArrayList<>();
        double melhorPontuacao = 0;

        for (Candidata candidata : candidatas) {
            int comum = palavrasEmComum(candidata.nome(), procurado);
            int tamanhoA = candidata.nome().palavrasBase().size();
            int tamanhoB = procurado.palavrasBase().size();

            boolean contido = comum > 0 && comum == Math.min(tamanhoA, tamanhoB);
            double pontuacao = comum == 0 ? 0 : (double) comum / (tamanhoA + tamanhoB - comum);

            if (pontuacao <= 0.5 && !contido) {
                continue;
            }

            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhores.clear();
            }

            if (pontuacao == melhorPontuacao) {
                melhores.add(candidata.escola());
            }
        }

        return melhores;
    }

    /** Palavras de {@code a} com uma correspondente semelhante em {@code b}. */
    private static int palavrasEmComum(NomeEscola a, NomeEscola b) {
        int comum = 0;

        for (String palavra : a.palavrasBase()) {
            if (b.palavrasBase().stream().anyMatch(outra -> palavrasSemelhantes(palavra, outra))) {
                comum++;
            }
        }

        return comum;
    }

    /** Iguais, ou com uma letra de diferença quando ambas têm ao menos 5 letras. */
    static boolean palavrasSemelhantes(String a, String b) {
        if (a.equals(b)) {
            return true;
        }

        if (Math.min(a.length(), b.length()) < 5 || Math.abs(a.length() - b.length()) > 1) {
            return false;
        }

        int[] anterior = new int[b.length() + 1];
        int[] atual = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            anterior[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            atual[0] = i;

            for (int j = 1; j <= b.length(); j++) {
                int custo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                atual[j] = Math.min(Math.min(atual[j - 1] + 1, anterior[j] + 1), anterior[j - 1] + custo);
            }

            int[] troca = anterior;
            anterior = atual;
            atual = troca;
        }

        return anterior[b.length()] <= 1;
    }

    private static boolean mesmaAldeia(Escola escola, String aldeiaProcurada) {
        return aldeiaProcurada != null
                && escola.getAldeia() != null
                && aldeiaProcurada.equals(normalizarNome(escola.getAldeia().getNome()));
    }

    /** Uma candidata é a escola; várias são desempatadas pela aldeia. */
    private static Escola desempatar(List<Escola> candidatas, String aldeiaProcurada) {
        if (candidatas.size() == 1) {
            return candidatas.getFirst();
        }

        List<Escola> mesmaAldeia = candidatas.stream()
                .filter(escola -> mesmaAldeia(escola, aldeiaProcurada))
                .toList();

        return mesmaAldeia.size() == 1 ? mesmaAldeia.getFirst() : null;
    }

    /** Sem acentos, em maiúsculas, só letras, números e espaços simples. */
    static String normalizarNome(String texto) {
        String normalizado = ExcelUtils.normalizar(texto);

        if (normalizado == null) {
            return "";
        }

        return normalizado
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Nome da escola reduzido para comparação. {@code base} não tem prefixos
     * ("ESC. IND.") nem o sufixo de anexo; {@code completo} inclui o sufixo e o
     * número do anexo ("ANEXO II"). Uma escola é anexo pelo nome ou, se já
     * cadastrada, pelo tipo de unidade, porque há anexos cadastrados sem o sufixo.
     */
    record NomeEscola(String base, String completo, boolean anexo, Set<String> palavrasBase) {

        static NomeEscola de(String nome, boolean anexoCadastrado) {
            List<String> palavras = new ArrayList<>();

            for (String palavra : normalizarNome(nome).split(" ")) {
                if (!palavra.isBlank() && !PALAVRAS_IGNORADAS.contains(palavra)) {
                    palavras.add(ABREVIACOES.getOrDefault(palavra, palavra));
                }
            }

            int posicaoAnexo = palavras.indexOf("ANEXO");
            List<String> base = posicaoAnexo < 0 ? palavras : palavras.subList(0, posicaoAnexo);

            return new NomeEscola(
                    String.join(" ", base),
                    String.join(" ", palavras),
                    anexoCadastrado || posicaoAnexo >= 0,
                    new HashSet<>(base)
            );
        }
    }

    /** "NEE SEE", "NEE CRUZEIRO DO SUL": lotação no núcleo de educação do município. */
    private static boolean ehNucleo(String lotacao) {
        return normalizarNome(lotacao).split(" ")[0].equals("NEE");
    }

    private static boolean mesmoId(Object entidadeGravada, Object entidade) {
        if (entidadeGravada == null || entidade == null) {
            return entidadeGravada == entidade;
        }

        return Objects.equals(id(entidadeGravada), id(entidade));
    }

    private static Long id(Object entidade) {
        return switch (entidade) {
            case Professor professor -> professor.getId();
            case Escola escola -> escola.getId();
            case Municipio municipio -> municipio.getId();
            default -> throw new IllegalStateException("Entidade sem comparação: " + entidade.getClass());
        };
    }

    /** CPF gravado como número perde os zeros à esquerda; aqui eles são restaurados. */
    private static String lerCpf(Row linha) {
        Cell celula = linha.getCell(COL_CPF, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (celula != null && celula.getCellType() == CellType.NUMERIC) {
            return String.format("%011d", (long) celula.getNumericCellValue());
        }

        return ExcelUtils.lerTexto(linha, COL_CPF);
    }

    private static <T> void atualizarSePreenchido(T valor, Consumer<T> atribuir) {
        if (valor != null) {
            atribuir.accept(valor);
        }
    }

    /** Linhas só com o número de ordem (sem município, lotação e nome) são ignoradas. */
    private static boolean semConteudo(Row linha) {
        return ExcelUtils.lerTexto(linha, COL_MUNICIPIO) == null
                && ExcelUtils.lerTexto(linha, COL_LOTACAO) == null
                && ExcelUtils.lerTexto(linha, COL_NOME) == null;
    }

    private static void validarCabecalho(Row cabecalho) {
        boolean layoutEsperado =
                "LOTACAO".equals(normalizarNome(ExcelUtils.lerTexto(cabecalho, COL_LOTACAO)))
                        && "NOME".equals(normalizarNome(ExcelUtils.lerTexto(cabecalho, COL_NOME)))
                        && "CPF".equals(normalizarNome(ExcelUtils.lerTexto(cabecalho, COL_CPF)));

        if (!layoutEsperado) {
            throw new IllegalArgumentException(
                    "A planilha não está no layout do quadro de professores: " +
                            "a linha 1 deve ter LOTACAO na coluna C, NOME na coluna I e CPF na coluna O."
            );
        }
    }

    private static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }

        return valor;
    }

    private static void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione a planilha do quadro de professores.");
        }

        String nome = arquivo.getOriginalFilename();

        if (nome == null || !nome.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("O arquivo deve estar no formato .xlsx.");
        }
    }

    private record DadosLinha(
            Municipio municipio,
            Escola escola,
            boolean vaga,
            String nome,
            String cpf,
            String matricula,
            String numeroContrato,
            String etnia,
            String sexo,
            String formacao,
            String cargo,
            String segmento,
            LocalDate inicioContrato,
            String amparoLegal,
            String tipoContrato
    ) {
    }

    /** Dados carregados uma vez por importação. */
    private final class Contexto {

        final AnoReferencia anoReferencia;
        final Map<String, Municipio> municipiosPorNome = new HashMap<>();
        final Map<Long, List<Escola>> escolasPorMunicipio = new HashMap<>();
        final Map<String, List<Professor>> professoresPorNome = new HashMap<>();
        final List<LotacaoProfessor> lotacoesExistentes;
        final Set<LotacaoProfessor> lotacoesUsadas = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Professor> professoresCriados = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Professor> professoresAtualizados = Collections.newSetFromMap(new IdentityHashMap<>());

        Contexto(AnoReferencia anoReferencia) {
            this.anoReferencia = anoReferencia;

            for (Municipio municipio : municipioRepository.findAll()) {
                municipiosPorNome.put(ExcelUtils.normalizar(municipio.getNome()), municipio);
            }

            for (Escola escola : escolaRepository.findAll()) {
                Municipio municipio = LotacaoProfessorService.municipioDaEscola(escola);

                if (municipio != null) {
                    escolasPorMunicipio.computeIfAbsent(municipio.getId(), id -> new ArrayList<>()).add(escola);
                }
            }

            for (Professor professor : professorRepository.findAll()) {
                professoresPorNome
                        .computeIfAbsent(ExcelUtils.normalizar(professor.getNome()), chave -> new ArrayList<>())
                        .add(professor);
            }

            lotacoesExistentes = lotacaoRepository.buscarPorAno(anoReferencia.getAno());
        }
    }
}
