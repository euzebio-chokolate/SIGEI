package ac.gov.br.sigei_backend.service.auditoria;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.CampoAlteradoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;
import ac.gov.br.sigei_backend.dto.response.historico.PaginaAuditoriaDTO;
import ac.gov.br.sigei_backend.mappers.EscolaMapper;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.service.CadastroEscolaService;
import ac.gov.br.sigei_backend.service.VinculoSedeService;
import ac.gov.br.sigei_backend.service.armazenamento.CloudinaryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O Envers grava as revisões no commit; por isso o teste usa transações reais.
 * As revisões se acumulam no banco entre testes, então cada teste usa um autor
 * próprio e filtra por ele.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({AuditoriaService.class, CadastroEscolaService.class, VinculoSedeService.class,
        AuditoriaServiceTest.MappersConfig.class})
class AuditoriaServiceTest {
    @TestConfiguration
    @ComponentScan(basePackageClasses = EscolaMapper.class)
    static class MappersConfig {}

    private static final AtomicLong PROXIMO_AUTOR = new AtomicLong(90_000);

    @MockitoBean CloudinaryService cloudinaryService;

    @Autowired AuditoriaService auditoria;
    @Autowired CadastroEscolaService cadastro;
    @Autowired EscolaRepository escolas;
    @Autowired AldeiaRepository aldeias;
    @Autowired TerraIndigenaRepository terras;
    @Autowired MunicipioRepository municipios;
    @Autowired PovoRepository povos;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;
    @Autowired ProfessorRepository professores;

    private Long autor;
    private Aldeia aldeia;
    private AnoReferencia ano2025;

    @BeforeEach
    void preparar() {
        autor = PROXIMO_AUTOR.incrementAndGet();
        autenticar(autor);

        Municipio municipio = new Municipio();
        municipio.setNome("Municipio auditoria");
        municipios.save(municipio);
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra auditoria");
        terra.setMunicipio(municipio);
        terras.save(terra);
        Povo povo = new Povo();
        povo.setNome("Povo auditoria");
        povos.save(povo);
        aldeia = new Aldeia();
        aldeia.setNome("Aldeia auditoria");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeias.save(aldeia);
        ano2025 = new AnoReferencia();
        ano2025.setAno(2025);
        anos.save(ano2025);
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        registros.deleteAll();
        escolas.deleteAll();
        professores.deleteAll();
        aldeias.deleteAll();
        povos.deleteAll();
        terras.deleteAll();
        municipios.deleteAll();
        anos.deleteAll();
    }

    @Test
    void deveListarAlteracoesDeTodasAsEntidadesComDescricaoDoRegistro() throws Exception {
        Long escolaId = cadastro.salvar(dados(null, "Escola auditada", 10), null).escola().id();
        cadastro.editar(escolaId, dados(escolaId, "Escola auditada", 12), null);

        List<AlteracaoHistoricoDTO> alteracoes = buscar(filtro(null, null), 0, 100).alteracoes();

        // Entidades fora da escola também aparecem
        AlteracaoHistoricoDTO criacaoAldeia = unica(alteracoes, "Aldeia", OperacaoHistorico.CRIACAO);
        assertEquals("Aldeia auditoria", criacaoAldeia.descricao());
        assertEquals(autor, criacaoAldeia.usuarioId());

        // Partes do registro anual são descritas pela escola e pelo ano
        AlteracaoHistoricoDTO totalAlunos = unica(alteracoes, "RegistroAnual", OperacaoHistorico.ALTERACAO);
        assertEquals("Escola auditada", totalAlunos.descricao());
        assertEquals(2025, totalAlunos.ano());
        assertEquals(List.of(new CampoAlteradoDTO("totalAlunos", 10, 12)), totalAlunos.campos());

        AlteracaoHistoricoDTO serie = unica(alteracoes, "MatriculaSerie", OperacaoHistorico.CRIACAO);
        assertEquals("Escola auditada · 1º ano", serie.descricao());
        assertEquals(2025, serie.ano());

        // Mais recente primeiro
        assertEquals(totalAlunos.revisao(), alteracoes.getFirst().revisao());
        assertTrue(alteracoes.getFirst().revisao() > alteracoes.getLast().revisao());
    }

    @Test
    void naoDeveExporDadosSensiveisDoProfessor() {
        Professor professor = new Professor();
        professor.setNome("Professor auditado");
        professor.setCpf("52998224725");
        professor.setMatricula("MAT-123");
        professores.save(professor);
        professor.setMatricula("MAT-456");
        professores.save(professor);

        List<AlteracaoHistoricoDTO> alteracoes = buscar(filtro("Professor", null), 0, 100).alteracoes();

        AlteracaoHistoricoDTO criacao = unica(alteracoes, "Professor", OperacaoHistorico.CRIACAO);
        assertTrue(criacao.campos().contains(new CampoAlteradoDTO("cpf", null, VersoesAuditadas.VALOR_PROTEGIDO)));
        assertTrue(criacao.campos().contains(new CampoAlteradoDTO("nome", null, "Professor auditado")));

        // A troca da matrícula é registrada, sem os valores
        AlteracaoHistoricoDTO alteracao = unica(alteracoes, "Professor", OperacaoHistorico.ALTERACAO);
        assertEquals(List.of(new CampoAlteradoDTO("matricula", VersoesAuditadas.VALOR_PROTEGIDO, VersoesAuditadas.VALOR_PROTEGIDO)),
                alteracao.campos());
        assertFalse(alteracoes.toString().contains("52998224725"));
        assertFalse(alteracoes.toString().contains("MAT-"));
    }

    @Test
    void deveFiltrarPorEntidadeOperacaoEPeriodo() throws Exception {
        Long escolaId = cadastro.salvar(dados(null, "Escola filtrada", 10), null).escola().id();
        escolas.deleteById(escolaId);

        List<AlteracaoHistoricoDTO> exclusoes = buscar(new FiltroAuditoria("Escola", OperacaoHistorico.EXCLUSAO,
                autor, null, null, null), 0, 100).alteracoes();
        AlteracaoHistoricoDTO exclusao = unica(exclusoes, "Escola", OperacaoHistorico.EXCLUSAO);
        assertEquals("Escola filtrada", exclusao.descricao());
        assertEquals(1, exclusoes.size());

        // Registro anual excluído em cascata: a escola é encontrada na última versão auditada
        AlteracaoHistoricoDTO registroExcluido = unica(buscar(new FiltroAuditoria("RegistroAnual", OperacaoHistorico.EXCLUSAO,
                autor, null, null, null), 0, 100).alteracoes(), "RegistroAnual", OperacaoHistorico.EXCLUSAO);
        assertEquals("Escola filtrada", registroExcluido.descricao());

        LocalDate amanha = LocalDate.now().plusDays(1);
        assertEquals(0, buscar(new FiltroAuditoria(null, null, autor, null, amanha, null), 0, 100).totalAlteracoes());
    }

    @Test
    void devePaginarPorAlteracao() throws Exception {
        cadastro.salvar(dados(null, "Escola paginada", 10), null);
        long total = buscar(filtro(null, null), 0, 100).totalAlteracoes();
        assertTrue(total > 3);

        PaginaAuditoriaDTO primeira = buscar(filtro(null, null), 0, 2);
        PaginaAuditoriaDTO segunda = buscar(filtro(null, null), 1, 2);

        assertEquals(2, primeira.alteracoes().size());
        assertEquals(total, primeira.totalAlteracoes());
        assertEquals((int) Math.ceil(total / 2.0), primeira.totalPaginas());
        assertNotEquals(primeira.alteracoes(), segunda.alteracoes());
        // Uma página além do fim vem vazia, sem erro
        assertTrue(buscar(filtro(null, null), 999, 2).alteracoes().isEmpty());
    }

    @Test
    void alteracoesDoSistemaDevemSerFiltradasSemAutor() {
        SecurityContextHolder.clearContext();
        Povo povo = new Povo();
        povo.setNome("Povo do sistema " + autor);
        povos.save(povo);

        List<AlteracaoHistoricoDTO> doSistema = auditoria.buscar(
                new FiltroAuditoria("Povo", OperacaoHistorico.CRIACAO, null, true, null, null), 0, 100).alteracoes();

        assertTrue(doSistema.stream().anyMatch(a -> ("Povo do sistema " + autor).equals(a.descricao())));
        assertTrue(doSistema.stream().allMatch(a -> a.usuarioId() == null));
    }

    @Test
    void deveRecusarFiltrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> buscar(filtro("Inexistente", null), 0, 20));
        assertThrows(IllegalArgumentException.class, () -> buscar(filtro(null, null), -1, 20));
        assertThrows(IllegalArgumentException.class, () -> buscar(filtro(null, null), 0, AuditoriaService.TAMANHO_MAXIMO + 1));
        assertThrows(IllegalArgumentException.class, () -> buscar(
                new FiltroAuditoria(null, null, null, null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)), 0, 20));
        assertThrows(IllegalArgumentException.class, () -> buscar(new FiltroAuditoria(null, null, 1L, true, null, null), 0, 20));
    }

    private PaginaAuditoriaDTO buscar(FiltroAuditoria filtro, int pagina, int tamanho) {
        return auditoria.buscar(filtro, pagina, tamanho);
    }

    private FiltroAuditoria filtro(String entidade, OperacaoHistorico operacao) {
        return new FiltroAuditoria(entidade, operacao, autor, null, null, null);
    }

    private static AlteracaoHistoricoDTO unica(List<AlteracaoHistoricoDTO> alteracoes, String entidade,
                                               OperacaoHistorico operacao) {
        List<AlteracaoHistoricoDTO> encontradas = alteracoes.stream()
                .filter(a -> a.entidade().equals(entidade) && a.operacao() == operacao)
                .toList();
        assertEquals(1, encontradas.size(), entidade + " " + operacao + ": " + alteracoes);
        return encontradas.getFirst();
    }

    private static void autenticar(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail("auditor" + id + "@sigei.com");
        usuario.setNome("Auditor " + id);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private CadastroEscolaRequestDTO dados(Long escolaId, String nome, int totalAlunos) {
        return new CadastroEscolaRequestDTO(
                new EscolaRequestDTO(escolaId, "COD-A", nome, "SEDE", null, aldeia.getId()),
                ano2025.getId(),
                "Estadual",
                "URBANA",
                totalAlunos,
                new CadastroEscolaRequestDTO.EquipeDTO(true, false, true, false, false, true),
                new CadastroEscolaRequestDTO.InfraestruturaDTO(2010, 3, -8.1, -70.5, "FUNCIONANDO", "FLUVIAL",
                        true, "POCO", false, "SOLAR", false),
                List.of(new CadastroEscolaRequestDTO.MatriculaNivelDTO("TOTAL_GERAL", 5, 5, 10)),
                List.of(new CadastroEscolaRequestDTO.MatriculaSerieDTO("1º ano", "FUNDAMENTAL", 2, 3, 5)),
                false
        );
    }
}
