package ac.gov.br.sigei_backend.service.relatorio;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.repository.RelatorioRepository;
import ac.gov.br.sigei_backend.repository.projection.*;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Coluna;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Indicador;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Secao;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Tabela;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Monta os relatórios exportáveis (indicadores, escolas, matrículas e
 * professores) de um ano, opcionalmente de um município, e os gera em planilha
 * ou PDF. Os números seguem as regras do painel: matrículas pelo total de alunos
 * do registro anual e energia presente quando o tipo não é "NAO".
 *
 * <p>CPF, matrícula e número do contrato nunca entram nos relatórios: as
 * consultas não selecionam esses campos.
 */
@Service
public class RelatorioService {

    private static final ZoneId FUSO = ZoneId.of("America/Rio_Branco");
    private static final DateTimeFormatter EMISSAO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String SEM_MUNICIPIO = "Sem município";
    private static final String NUCLEO = "Núcleo de educação";

    /** Ordem de exibição dos níveis e das séries. */
    private static final List<String> ORDEM_NIVEIS = List.of(
            "ENSINO_INFANTIL", "FUNDAMENTAL_ANOS_INICIAIS", "FUNDAMENTAL_ANOS_FINAIS", "ENSINO_MEDIO", "EJA", "AEE"
    );
    private static final List<String> ORDEM_SERIES = List.of(
            "1º ANO", "2º ANO", "3º ANO", "4º ANO", "5º ANO", "6º ANO", "7º ANO", "8º ANO", "9º ANO",
            "1ª SÉRIE", "2ª SÉRIE", "3ª SÉRIE"
    );

    private final RelatorioRepository relatorioRepository;
    private final AnoReferenciaRepository anoReferenciaRepository;
    private final MunicipioRepository municipioRepository;
    private final GeradorPlanilhaRelatorio geradorPlanilha;
    private final GeradorPdfRelatorio geradorPdf;

    public RelatorioService(
            RelatorioRepository relatorioRepository,
            AnoReferenciaRepository anoReferenciaRepository,
            MunicipioRepository municipioRepository,
            GeradorPlanilhaRelatorio geradorPlanilha,
            GeradorPdfRelatorio geradorPdf
    ) {
        this.relatorioRepository = relatorioRepository;
        this.anoReferenciaRepository = anoReferenciaRepository;
        this.municipioRepository = municipioRepository;
        this.geradorPlanilha = geradorPlanilha;
        this.geradorPdf = geradorPdf;
    }

    /**
     * @param ano         ano de referência; nulo usa o mais recente
     * @param municipioId município; nulo inclui todos
     */
    @Transactional(readOnly = true)
    public ArquivoRelatorio gerar(TipoRelatorio tipo, FormatoRelatorio formato, Integer ano, Long municipioId) {
        Filtro filtro = new Filtro(resolverAno(ano), resolverMunicipio(municipioId));
        Relatorio relatorio = montar(tipo, filtro);
        String emitidoEm = ZonedDateTime.now(FUSO).format(EMISSAO);

        byte[] conteudo = formato == FormatoRelatorio.XLSX
                ? geradorPlanilha.gerar(relatorio, emitidoEm)
                : geradorPdf.gerar(relatorio, emitidoEm);

        return new ArquivoRelatorio(nomeArquivo(tipo, filtro, formato), formato.tipoMidia(), conteudo);
    }

    Relatorio montar(TipoRelatorio tipo, Filtro filtro) {
        return switch (tipo) {
            case PAINEL -> painel(filtro);
            case ESCOLAS -> escolas(filtro);
            case MATRICULAS -> matriculas(filtro);
            case PROFESSORES -> professores(filtro);
        };
    }

    // ------------------------------------------------------------------ Painel

    private Relatorio painel(Filtro filtro) {
        List<LinhaEscolaRelatorio> escolas = escolasDoFiltro(filtro);
        List<LotacaoRelatorio> lotacoes = lotacoesDoFiltro(filtro);
        List<LinhaEscolaRelatorio> avaliadas = escolas.stream().filter(e -> e.infraestruturaId() != null).toList();

        List<Indicador> resumo = new ArrayList<>(resumoEscolas(escolas));
        resumo.add(new Indicador("Municípios", contarDistintos(escolas, LinhaEscolaRelatorio::municipioId)));
        resumo.add(new Indicador("Terras indígenas",
                contarDistintos(escolas, e -> e.terraIndigena() == null ? null : e.municipioId() + "|" + e.terraIndigena())));
        resumo.add(new Indicador("Povos", contarDistintos(escolas, LinhaEscolaRelatorio::povo)));
        resumo.addAll(resumoLotacoes(lotacoes));

        Map<String, Long> porNivel = somarNiveis(niveisDoFiltro(filtro, escolas).values());
        long totalNiveis = porNivel.values().stream().mapToLong(Long::longValue).sum();

        List<List<Object>> linhasNivel = porNivel.entrySet().stream()
                .map(n -> linha(RotulosRelatorio.nivel(n.getKey()), n.getValue(), percentual(n.getValue(), totalNiveis)))
                .toList();

        long internet = avaliadas.stream().filter(e -> Boolean.TRUE.equals(e.temInternet())).count();
        long energia = avaliadas.stream().filter(e -> temEnergia(e.tipoEnergia())).count();
        long predio = avaliadas.stream().filter(e -> Boolean.TRUE.equals(e.temPredio())).count();

        List<List<Object>> linhasInfra = List.of(
                linha("Com internet", internet, percentual(internet, avaliadas.size())),
                linha("Com energia elétrica", energia, percentual(energia, avaliadas.size())),
                linha("Com prédio escolar", predio, percentual(predio, avaliadas.size()))
        );

        Map<String, Long> porSituacao = contarPor(avaliadas,
                e -> Objects.requireNonNullElse(RotulosRelatorio.situacao(e.situacao()), "Não informada"));

        List<List<Object>> linhasSituacao = ordenarPorValor(porSituacao).stream()
                .map(s -> linha(s.getKey(), s.getValue(), percentual(s.getValue(), avaliadas.size())))
                .toList();

        return new Relatorio("Indicadores consolidados", filtro.descricao(), false, List.of(
                Secao.deIndicadores("Resumo", resumo),
                Secao.deTabela("Matrículas por nível", new Tabela(
                        List.of(Coluna.texto("Nível", 3), Coluna.numero("Matrículas"), Coluna.percentual("%")),
                        linhasNivel,
                        linha("Total", totalNiveis, linhasNivel.isEmpty() ? null : 100.0))),
                new Secao("Infraestrutura",
                        List.of(new Indicador("Escolas com infraestrutura informada", avaliadas.size())),
                        new Tabela(
                                List.of(Coluna.texto("Item", 3), Coluna.numero("Escolas"), Coluna.percentual("% das escolas")),
                                linhasInfra)),
                Secao.deTabela("Situação das escolas", new Tabela(
                        List.of(Coluna.texto("Situação", 3), Coluna.numero("Escolas"), Coluna.percentual("%")),
                        linhasSituacao)),
                Secao.deTabela("Por município", tabelaPorMunicipio(escolas, lotacoes)),
                Secao.deTabela("Evolução histórica", tabelaEvolucao(filtro))
        ));
    }

    private Tabela tabelaPorMunicipio(List<LinhaEscolaRelatorio> escolas, List<LotacaoRelatorio> lotacoes) {
        Map<String, List<LinhaEscolaRelatorio>> escolasPorMunicipio = agrupar(escolas,
                e -> Objects.requireNonNullElse(e.municipio(), SEM_MUNICIPIO));
        Map<String, List<LotacaoRelatorio>> lotacoesPorMunicipio = agrupar(lotacoes, LotacaoRelatorio::municipio);

        SortedSet<String> municipios = new TreeSet<>(escolasPorMunicipio.keySet());
        municipios.addAll(lotacoesPorMunicipio.keySet());

        List<List<Object>> linhas = new ArrayList<>();
        long[] totais = new long[4];

        for (String municipio : municipios) {
            List<LinhaEscolaRelatorio> doMunicipio = escolasPorMunicipio.getOrDefault(municipio, List.of());
            List<LotacaoRelatorio> lotacoesDoMunicipio = lotacoesPorMunicipio.getOrDefault(municipio, List.of());

            long[] valores = {
                    doMunicipio.size(),
                    somarAlunos(doMunicipio),
                    lotacoesDoMunicipio.size(),
                    lotacoesDoMunicipio.stream().filter(l -> l.professorId() == null).count()
            };

            for (int i = 0; i < totais.length; i++) {
                totais[i] += valores[i];
            }

            linhas.add(linha(municipio, valores[0], valores[1], valores[2], valores[3]));
        }

        return new Tabela(
                List.of(Coluna.texto("Município", 3), Coluna.numero("Escolas"), Coluna.numero("Matrículas"),
                        Coluna.numero("Lotações"), Coluna.numero("Vagas em aberto")),
                linhas,
                linha("Total", totais[0], totais[1], totais[2], totais[3]));
    }

    private Tabela tabelaEvolucao(Filtro filtro) {
        Map<Integer, long[]> porAno = new TreeMap<>();

        for (EvolucaoRelatorio item : relatorioRepository.buscarEvolucao()) {
            if (filtro.municipio() == null || filtro.municipio().getId().equals(item.municipioId())) {
                long[] valores = porAno.computeIfAbsent(item.ano(), ano -> new long[2]);
                valores[0] += item.escolas();
                valores[1] += item.matriculas();
            }
        }

        return new Tabela(
                List.of(Coluna.texto("Ano", 1), Coluna.numero("Escolas"), Coluna.numero("Matrículas")),
                porAno.entrySet().stream()
                        .map(ano -> linha(ano.getKey().toString(), ano.getValue()[0], ano.getValue()[1]))
                        .toList());
    }

    // ----------------------------------------------------------------- Escolas

    private Relatorio escolas(Filtro filtro) {
        List<LinhaEscolaRelatorio> escolas = escolasDoFiltro(filtro);

        List<List<Object>> linhas = escolas.stream()
                .map(e -> linha(
                        e.municipio(), e.terraIndigena(), e.aldeia(), e.povo(), e.codigo(), e.escola(),
                        RotulosRelatorio.tipoUnidade(e.tipoUnidade()), e.sede(),
                        RotulosRelatorio.situacao(e.situacao()), RotulosRelatorio.acesso(e.tipoAcesso()),
                        RotulosRelatorio.simNao(e.temPredio()), e.numSalas(), RotulosRelatorio.simNao(e.temInternet()),
                        RotulosRelatorio.agua(e.tipoAgua()), RotulosRelatorio.energia(e.tipoEnergia()), e.totalAlunos()))
                .toList();

        long salas = escolas.stream().map(LinhaEscolaRelatorio::numSalas).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();

        List<Object> total = new ArrayList<>(Collections.nCopies(16, null));
        total.set(0, "Total");
        total.set(11, salas);
        total.set(15, somarAlunos(escolas));

        return new Relatorio("Escolas e infraestrutura", filtro.descricao(), true, List.of(
                Secao.deIndicadores("Resumo", resumoEscolas(escolas)),
                Secao.deTabela("Escolas", new Tabela(
                        List.of(Coluna.texto("Município", 54), Coluna.texto("Terra indígena", 62),
                                Coluna.texto("Aldeia", 58), Coluna.texto("Povo", 58), Coluna.texto("Código INEP", 44),
                                Coluna.texto("Escola", 104), Coluna.texto("Tipo", 30), Coluna.texto("Escola-sede", 82),
                                Coluna.texto("Situação", 52), Coluna.texto("Acesso", 44), Coluna.texto("Prédio", 32),
                                Coluna.numero("Salas", 28), Coluna.texto("Internet", 38), Coluna.texto("Água", 46),
                                Coluna.texto("Energia", 46), Coluna.numero("Alunos", 34)),
                        linhas,
                        total))
        ));
    }

    // -------------------------------------------------------------- Matrículas

    private Relatorio matriculas(Filtro filtro) {
        List<LinhaEscolaRelatorio> escolas = escolasDoFiltro(filtro);
        Map<Long, Map<String, MatriculaNivelRelatorio>> niveis = niveisDoFiltro(filtro, escolas);

        List<Coluna> colunasNumericas = List.of(
                Coluna.numero("Infantil", 44), Coluna.numero("Anos iniciais", 44), Coluna.numero("Anos finais", 44),
                Coluna.numero("Médio", 40), Coluna.numero("EJA", 34), Coluna.numero("AEE", 34),
                Coluna.numero("Total", 40), Coluna.numero("Masc.", 38), Coluna.numero("Fem.", 38));

        List<List<Object>> porEscola = new ArrayList<>();
        Map<String, long[]> porMunicipio = new TreeMap<>();
        long[] totalGeral = new long[colunasNumericas.size()];

        for (LinhaEscolaRelatorio escola : escolas) {
            long[] valores = valoresMatricula(escola, niveis.getOrDefault(escola.registroId(), Map.of()));
            somar(totalGeral, valores);
            somar(porMunicipio.computeIfAbsent(Objects.requireNonNullElse(escola.municipio(), SEM_MUNICIPIO),
                    m -> new long[valores.length]), valores);

            List<Object> linha = new ArrayList<>(Arrays.asList(escola.municipio(), escola.codigo(), escola.escola()));
            Arrays.stream(valores).forEach(linha::add);
            porEscola.add(linha);
        }

        List<Coluna> colunasEscola = new ArrayList<>(List.of(
                Coluna.texto("Município", 62), Coluna.texto("Código INEP", 46), Coluna.texto("Escola", 150)));
        colunasEscola.addAll(colunasNumericas);

        List<Coluna> colunasMunicipio = new ArrayList<>(List.of(Coluna.texto("Município", 120)));
        colunasMunicipio.addAll(colunasNumericas);

        List<List<Object>> linhasMunicipio = porMunicipio.entrySet().stream()
                .map(m -> {
                    List<Object> linha = new ArrayList<>(List.of(m.getKey()));
                    Arrays.stream(m.getValue()).forEach(linha::add);
                    return linha;
                })
                .toList();

        return new Relatorio("Matrículas por etapa", filtro.descricao(), true, List.of(
                Secao.deIndicadores("Resumo", List.of(
                        new Indicador("Escolas", escolas.size()),
                        new Indicador("Matrículas", totalGeral[6]),
                        new Indicador("Masculino", totalGeral[7]),
                        new Indicador("Feminino", totalGeral[8]))),
                Secao.deTabela("Por escola", new Tabela(colunasEscola, porEscola, totalComRotulo(3, totalGeral))),
                Secao.deTabela("Por município", new Tabela(colunasMunicipio, linhasMunicipio, totalComRotulo(1, totalGeral))),
                Secao.deTabela("Por série (sem EJA e AEE)", tabelaSeries(filtro, escolas))
        ));
    }

    /**
     * Infantil, anos iniciais, anos finais, médio, EJA, AEE, total, masculino e
     * feminino. O total e a divisão por sexo vêm do nível TOTAL_GERAL; sem ele, o
     * total é o do registro anual.
     */
    private static long[] valoresMatricula(LinhaEscolaRelatorio escola, Map<String, MatriculaNivelRelatorio> niveis) {
        long[] valores = new long[9];

        for (int i = 0; i < ORDEM_NIVEIS.size(); i++) {
            MatriculaNivelRelatorio nivel = niveis.get(ORDEM_NIVEIS.get(i));
            valores[i] = nivel == null ? 0 : valor(nivel.total());
        }

        MatriculaNivelRelatorio total = niveis.get("TOTAL_GERAL");
        valores[6] = total != null && total.total() != null ? total.total() : valor(escola.totalAlunos());
        valores[7] = total == null ? 0 : valor(total.masculino());
        valores[8] = total == null ? 0 : valor(total.feminino());
        return valores;
    }

    private Tabela tabelaSeries(Filtro filtro, List<LinhaEscolaRelatorio> escolas) {
        Set<Long> registros = escolas.stream().map(LinhaEscolaRelatorio::registroId).collect(Collectors.toSet());
        Map<String, long[]> porSerie = new HashMap<>();
        Map<String, String> etapaDaSerie = new HashMap<>();

        for (MatriculaSerieRelatorio serie : relatorioRepository.buscarMatriculasPorSerie(filtro.ano())) {
            if (registros.contains(serie.registroId()) && serie.denominacao() != null) {
                porSerie.computeIfAbsent(serie.denominacao(), d -> new long[1])[0] += valor(serie.total());
                etapaDaSerie.putIfAbsent(serie.denominacao(), serie.etapaEnsino());
            }
        }

        Comparator<String> ordem = Comparator.<String>comparingInt(serie -> {
            int posicao = ORDEM_SERIES.indexOf(serie);
            return posicao < 0 ? Integer.MAX_VALUE : posicao;
        }).thenComparing(Comparator.naturalOrder());

        List<List<Object>> linhas = porSerie.keySet().stream()
                .sorted(ordem)
                .map(serie -> linha(RotulosRelatorio.nivel(etapaDaSerie.get(serie)), serie, porSerie.get(serie)[0]))
                .toList();

        long total = porSerie.values().stream().mapToLong(valores -> valores[0]).sum();

        return new Tabela(
                List.of(Coluna.texto("Etapa", 2), Coluna.texto("Série / ano", 1.2f), Coluna.numero("Matrículas")),
                linhas,
                linha("Total", null, total));
    }

    // ------------------------------------------------------------- Professores

    private Relatorio professores(Filtro filtro) {
        List<LotacaoRelatorio> lotacoes = lotacoesDoFiltro(filtro);

        List<List<Object>> linhas = lotacoes.stream()
                .map(l -> linha(
                        l.municipio(),
                        l.escola() == null ? NUCLEO : l.escola(),
                        l.professorId() == null ? "Vaga em aberto" : l.professor(),
                        l.etnia(), RotulosRelatorio.sexo(l.sexo()), l.formacao(), l.cargo(), l.segmento(),
                        RotulosRelatorio.contrato(l.tipoContrato()),
                        l.inicioContrato() == null ? null : l.inicioContrato().format(DATA)))
                .toList();

        Map<String, Long> porContrato = contarPor(lotacoes,
                l -> Objects.requireNonNullElse(RotulosRelatorio.contrato(l.tipoContrato()), "Não informado"));

        Map<String, List<LotacaoRelatorio>> porMunicipio = agrupar(lotacoes, LotacaoRelatorio::municipio);
        long[] totais = new long[4];

        List<List<Object>> linhasMunicipio = new TreeMap<>(porMunicipio).entrySet().stream()
                .map(m -> {
                    long[] valores = {
                            m.getValue().size(),
                            contarDistintos(m.getValue(), LotacaoRelatorio::professorId),
                            m.getValue().stream().filter(l -> l.professorId() == null).count(),
                            m.getValue().stream().filter(l -> l.escola() == null).count()
                    };
                    somar(totais, valores);
                    return linha(m.getKey(), valores[0], valores[1], valores[2], valores[3]);
                })
                .toList();

        List<Indicador> resumo = new ArrayList<>(resumoLotacoes(lotacoes));
        resumo.add(new Indicador("Lotações no núcleo", lotacoes.stream().filter(l -> l.escola() == null).count()));

        return new Relatorio("Professores e lotações", filtro.descricao(), true, List.of(
                Secao.deIndicadores("Resumo", resumo),
                Secao.deTabela("Lotações", new Tabela(
                        List.of(Coluna.texto("Município", 62), Coluna.texto("Lotação", 120),
                                Coluna.texto("Professor", 112), Coluna.texto("Etnia", 62), Coluna.texto("Sexo", 46),
                                Coluna.texto("Formação", 92), Coluna.texto("Cargo", 92), Coluna.texto("Segmento", 66),
                                Coluna.texto("Contrato", 52), Coluna.texto("Início", 46)),
                        linhas)),
                Secao.deTabela("Por tipo de contrato", new Tabela(
                        List.of(Coluna.texto("Tipo de contrato", 3), Coluna.numero("Lotações"), Coluna.percentual("%")),
                        ordenarPorValor(porContrato).stream()
                                .map(c -> linha(c.getKey(), c.getValue(), percentual(c.getValue(), lotacoes.size())))
                                .toList())),
                Secao.deTabela("Por município", new Tabela(
                        List.of(Coluna.texto("Município", 3), Coluna.numero("Lotações"), Coluna.numero("Professores"),
                                Coluna.numero("Vagas em aberto"), Coluna.numero("No núcleo")),
                        linhasMunicipio,
                        linha("Total", totais[0], contarDistintos(lotacoes, LotacaoRelatorio::professorId), totais[2], totais[3])))
        ));
    }

    // ------------------------------------------------------------------- Dados

    private List<LinhaEscolaRelatorio> escolasDoFiltro(Filtro filtro) {
        return relatorioRepository.buscarEscolas(filtro.ano()).stream()
                .filter(e -> filtro.inclui(e.municipioId()))
                .toList();
    }

    private List<LotacaoRelatorio> lotacoesDoFiltro(Filtro filtro) {
        return relatorioRepository.buscarLotacoes(filtro.ano()).stream()
                .filter(l -> filtro.inclui(l.municipioId()))
                .toList();
    }

    /** Níveis de matrícula por registro anual, só das escolas do filtro. */
    private Map<Long, Map<String, MatriculaNivelRelatorio>> niveisDoFiltro(Filtro filtro, List<LinhaEscolaRelatorio> escolas) {
        Set<Long> registros = escolas.stream().map(LinhaEscolaRelatorio::registroId).collect(Collectors.toSet());
        Map<Long, Map<String, MatriculaNivelRelatorio>> niveis = new HashMap<>();

        for (MatriculaNivelRelatorio nivel : relatorioRepository.buscarMatriculasPorNivel(filtro.ano())) {
            if (registros.contains(nivel.registroId()) && nivel.nivel() != null) {
                niveis.computeIfAbsent(nivel.registroId(), id -> new HashMap<>()).putIfAbsent(nivel.nivel(), nivel);
            }
        }

        return niveis;
    }

    /** Soma por nível, na ordem de exibição, sem o TOTAL_GERAL. */
    private static Map<String, Long> somarNiveis(Collection<Map<String, MatriculaNivelRelatorio>> niveis) {
        Map<String, Long> soma = new LinkedHashMap<>();
        ORDEM_NIVEIS.forEach(nivel -> soma.put(nivel, 0L));

        for (Map<String, MatriculaNivelRelatorio> doRegistro : niveis) {
            for (MatriculaNivelRelatorio nivel : doRegistro.values()) {
                if (soma.containsKey(nivel.nivel())) {
                    soma.merge(nivel.nivel(), (long) valor(nivel.total()), Long::sum);
                }
            }
        }

        return soma;
    }

    private static List<Indicador> resumoEscolas(List<LinhaEscolaRelatorio> escolas) {
        long anexos = escolas.stream().filter(e -> "ANEXO".equalsIgnoreCase(e.tipoUnidade())).count();

        return List.of(
                new Indicador("Escolas", escolas.size()),
                new Indicador("Sedes", escolas.size() - anexos),
                new Indicador("Anexos", anexos),
                new Indicador("Matrículas", somarAlunos(escolas))
        );
    }

    private static List<Indicador> resumoLotacoes(List<LotacaoRelatorio> lotacoes) {
        return List.of(
                new Indicador("Professores lotados", contarDistintos(lotacoes, LotacaoRelatorio::professorId)),
                new Indicador("Lotações", lotacoes.size()),
                new Indicador("Vagas em aberto", lotacoes.stream().filter(l -> l.professorId() == null).count())
        );
    }

    // ------------------------------------------------------------------- Apoio

    private int resolverAno(Integer ano) {
        if (ano != null) {
            return anoReferenciaRepository.findByAno(ano)
                    .map(AnoReferencia::getAno)
                    .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado: " + ano));
        }

        return anoReferenciaRepository.findTopByOrderByAnoDesc()
                .map(AnoReferencia::getAno)
                .orElseThrow(() -> new NoSuchElementException("Nenhum ano de referência cadastrado"));
    }

    private Municipio resolverMunicipio(Long municipioId) {
        if (municipioId == null) {
            return null;
        }

        return municipioRepository.findById(municipioId)
                .orElseThrow(() -> new NoSuchElementException("Município não encontrado: " + municipioId));
    }

    private static String nomeArquivo(TipoRelatorio tipo, Filtro filtro, FormatoRelatorio formato) {
        String municipio = filtro.municipio() == null
                ? ""
                : "-" + ExcelUtils.normalizar(filtro.municipio().getNome()).toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");

        return "sigei-" + tipo.chave() + "-" + filtro.ano() + municipio + "." + formato.extensao();
    }

    private static boolean temEnergia(String tipoEnergia) {
        return tipoEnergia != null && !tipoEnergia.isBlank() && !"NAO".equalsIgnoreCase(tipoEnergia.trim());
    }

    private static long somarAlunos(List<LinhaEscolaRelatorio> escolas) {
        return escolas.stream().mapToLong(e -> valor(e.totalAlunos())).sum();
    }

    private static <T> long contarDistintos(List<T> itens, Function<T, Object> chave) {
        return itens.stream().map(chave).filter(Objects::nonNull).distinct().count();
    }

    private static <T> Map<String, Long> contarPor(List<T> itens, Function<T, String> chave) {
        return itens.stream().collect(Collectors.groupingBy(chave, Collectors.counting()));
    }

    private static <T> Map<String, List<T>> agrupar(List<T> itens, Function<T, String> chave) {
        return itens.stream().collect(Collectors.groupingBy(chave));
    }

    private static List<Map.Entry<String, Long>> ordenarPorValor(Map<String, Long> contagem) {
        return contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    /** De 0 a 100, com uma casa decimal; nulo quando o total é zero. */
    static Double percentual(long parte, long total) {
        return total == 0 ? null : Math.round(parte * 1000.0 / total) / 10.0;
    }

    private static List<Object> totalComRotulo(int colunasDeTexto, long[] valores) {
        List<Object> total = new ArrayList<>();
        total.add("Total");

        for (int i = 1; i < colunasDeTexto; i++) {
            total.add(null);
        }

        Arrays.stream(valores).forEach(total::add);
        return total;
    }

    private static void somar(long[] acumulado, long[] valores) {
        for (int i = 0; i < acumulado.length; i++) {
            acumulado[i] += valores[i];
        }
    }

    private static int valor(Integer valor) {
        return valor == null ? 0 : valor;
    }

    /** Linha de tabela que aceita nulos (List.of não aceita). */
    private static List<Object> linha(Object... valores) {
        return Arrays.asList(valores);
    }

    /** Ano já resolvido e município opcional. */
    record Filtro(int ano, Municipio municipio) {

        boolean inclui(Long municipioId) {
            return municipio == null || municipio.getId().equals(municipioId);
        }

        String descricao() {
            return "Ano de referência: " + ano + " · " +
                    (municipio == null ? "Todos os municípios" : "Município: " + municipio.getNome());
        }
    }
}
