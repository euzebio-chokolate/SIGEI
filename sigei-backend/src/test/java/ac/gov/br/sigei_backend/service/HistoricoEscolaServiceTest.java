package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.CampoAlteradoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;
import ac.gov.br.sigei_backend.mappers.EscolaMapper;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
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

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O Envers grava as revisões no commit; por isso o teste usa transações reais,
 * sem a transação revertida do {@code @DataJpaTest}.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({CadastroEscolaService.class, VinculoSedeService.class, HistoricoEscolaService.class,
        HistoricoEscolaServiceTest.MappersConfig.class})
class HistoricoEscolaServiceTest {
    @TestConfiguration
    @ComponentScan(basePackageClasses = EscolaMapper.class)
    static class MappersConfig {}

    @MockitoBean CloudinaryService cloudinaryService;

    @Autowired HistoricoEscolaService historico;
    @Autowired CadastroEscolaService cadastro;
    @Autowired EscolaRepository escolas;
    @Autowired AldeiaRepository aldeias;
    @Autowired TerraIndigenaRepository terras;
    @Autowired MunicipioRepository municipios;
    @Autowired PovoRepository povos;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;

    private Aldeia aldeia;
    private AnoReferencia ano2025;

    @BeforeEach
    void preparar() {
        Municipio municipio = new Municipio();
        municipio.setNome("Municipio historico");
        municipios.save(municipio);
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra historico");
        terra.setMunicipio(municipio);
        terras.save(terra);
        Povo povo = new Povo();
        povo.setNome("Povo historico");
        povos.save(povo);
        aldeia = new Aldeia();
        aldeia.setNome("Aldeia historico");
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
        aldeias.deleteAll();
        povos.deleteAll();
        terras.deleteAll();
        municipios.deleteAll();
        anos.deleteAll();
    }

    @Test
    void deveRegistrarAutorDataECamposAlteradosDeCadaRevisao() throws Exception {
        autenticar(7L, "gestora@sigei.com", "Gestora");
        Long escolaId = cadastro.salvar(dados(null, "Escola original", 10, 3), null).escola().id();
        cadastro.editar(dados(escolaId, "Escola renomeada", 12, 3), null);

        List<AlteracaoHistoricoDTO> alteracoes = historico.buscarPorEscola(escolaId);

        AlteracaoHistoricoDTO renomeacao = unica(alteracoes, "Escola", OperacaoHistorico.ALTERACAO);
        assertEquals(7L, renomeacao.usuarioId());
        assertEquals("gestora@sigei.com", renomeacao.usuarioEmail());
        assertEquals("Gestora", renomeacao.usuarioNome());
        assertNotNull(renomeacao.dataHora());
        assertNull(renomeacao.ano());
        assertEquals(List.of(new CampoAlteradoDTO("nome", "Escola original", "Escola renomeada")),
                renomeacao.campos());

        AlteracaoHistoricoDTO totalAlunos = unica(alteracoes, "RegistroAnual", OperacaoHistorico.ALTERACAO);
        assertEquals(2025, totalAlunos.ano());
        assertEquals(List.of(new CampoAlteradoDTO("totalAlunos", 10, 12)), totalAlunos.campos());

        // A infraestrutura não mudou na edição: só a criação aparece
        assertEquals(1, alteracoes.stream().filter(a -> a.entidade().equals("InfraestruturaEscola")).count());
        AlteracaoHistoricoDTO criacaoInfra = unica(alteracoes, "InfraestruturaEscola", OperacaoHistorico.CRIACAO);
        assertTrue(criacaoInfra.campos().contains(new CampoAlteradoDTO("numSalas", null, 3)));
        assertEquals(2025, criacaoInfra.ano());

        assertTrue(alteracoes.stream().anyMatch(a -> a.entidade().equals("EquipeEscola")));
        assertTrue(alteracoes.stream().anyMatch(a -> a.entidade().equals("MatriculaNivel")));
        assertTrue(alteracoes.stream().anyMatch(a -> a.entidade().equals("MatriculaSerie")));

        // Mais recente primeiro
        assertEquals(renomeacao.revisao(), alteracoes.getFirst().revisao());
        assertTrue(alteracoes.getFirst().revisao() > alteracoes.getLast().revisao());
    }

    @Test
    void alteracaoSemUsuarioAutenticadoDeveSerAtribuidaAoSistema() throws Exception {
        Long escolaId = cadastro.salvar(dados(null, "Escola do sistema", 10, 3), null).escola().id();

        AlteracaoHistoricoDTO criacao = unica(historico.buscarPorEscola(escolaId), "Escola", OperacaoHistorico.CRIACAO);

        assertNull(criacao.usuarioId());
        assertNull(criacao.usuarioEmail());
    }

    @Test
    void historicoDeveSobreviverAExclusaoDaEscola() throws Exception {
        autenticar(7L, "gestora@sigei.com", "Gestora");
        Long escolaId = cadastro.salvar(dados(null, "Escola excluida", 10, 3), null).escola().id();
        escolas.deleteById(escolaId);

        List<AlteracaoHistoricoDTO> alteracoes = historico.buscarPorEscola(escolaId);

        AlteracaoHistoricoDTO exclusao = unica(alteracoes, "Escola", OperacaoHistorico.EXCLUSAO);
        assertTrue(exclusao.campos().contains(new CampoAlteradoDTO("nome", "Escola excluida", null)));
        assertEquals("gestora@sigei.com", exclusao.usuarioEmail());
        // A cascata também removeu o registro anual, que aparece como excluído
        unica(alteracoes, "RegistroAnual", OperacaoHistorico.EXCLUSAO);
    }

    @Test
    void escolaSemHistoricoDeveResultarEmNaoEncontrada() {
        assertThrows(NoSuchElementException.class, () -> historico.buscarPorEscola(999_999L));
    }

    private static AlteracaoHistoricoDTO unica(List<AlteracaoHistoricoDTO> alteracoes, String entidade,
                                               OperacaoHistorico operacao) {
        List<AlteracaoHistoricoDTO> encontradas = alteracoes.stream()
                .filter(a -> a.entidade().equals(entidade) && a.operacao() == operacao)
                .toList();
        assertEquals(1, encontradas.size(), entidade + " " + operacao + ": " + alteracoes);
        return encontradas.getFirst();
    }

    private static void autenticar(Long id, String email, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setNome(nome);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private CadastroEscolaRequestDTO dados(Long escolaId, String nome, int totalAlunos, int numSalas) {
        return new CadastroEscolaRequestDTO(
                new EscolaRequestDTO(escolaId, "COD-H", nome, "SEDE", null, aldeia.getId()),
                ano2025.getId(),
                "Estadual",
                "Prédio próprio",
                totalAlunos,
                new CadastroEscolaRequestDTO.EquipeDTO(true, false, true, false, false, true),
                new CadastroEscolaRequestDTO.InfraestruturaDTO(2010, numSalas, -8.1, -70.5, "ATIVA", "FLUVIAL",
                        true, "POCO", false, "SOLAR", false),
                List.of(new CadastroEscolaRequestDTO.MatriculaNivelDTO("TOTAL_GERAL", 5, 5, 10)),
                List.of(new CadastroEscolaRequestDTO.MatriculaSerieDTO("1º ano", "FUNDAMENTAL", 2, 3, 5)),
                false
        );
    }
}
