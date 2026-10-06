package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.CadastroEscolaResponseDTO;
import ac.gov.br.sigei_backend.exceptions.ArmazenamentoImagemNaoConfiguradoException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Executa o service com transações reais (sem a transação de teste do
 * {@code @DataJpaTest}) para verificar que falhas desfazem todo o cadastro.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({CadastroEscolaService.class, VinculoSedeService.class, CadastroEscolaServiceTest.MappersConfig.class})
class CadastroEscolaServiceTest {
    @TestConfiguration
    @ComponentScan(basePackageClasses = EscolaMapper.class)
    static class MappersConfig {}

    @MockitoBean CloudinaryService cloudinaryService;

    @Autowired CadastroEscolaService service;
    @Autowired EscolaRepository escolas;
    @Autowired AldeiaRepository aldeias;
    @Autowired TerraIndigenaRepository terras;
    @Autowired MunicipioRepository municipios;
    @Autowired PovoRepository povos;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;
    @Autowired EquipeEscolaRepository equipes;
    @Autowired InfraestruturaEscolaRepository infraestruturas;
    @Autowired MatriculaNivelRepository matriculasNivel;
    @Autowired MatriculaSerieRepository matriculasSerie;

    private Aldeia aldeia;
    private AnoReferencia ano2025;
    private AnoReferencia ano2026;

    @BeforeEach
    void preparar() {
        Municipio municipio = new Municipio();
        municipio.setNome("Municipio cadastro");
        municipios.save(municipio);
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra cadastro");
        terra.setMunicipio(municipio);
        terras.save(terra);
        Povo povo = new Povo();
        povo.setNome("Povo cadastro");
        povos.save(povo);
        aldeia = new Aldeia();
        aldeia.setNome("Aldeia cadastro");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeias.save(aldeia);
        ano2025 = criarAno(2025);
        ano2026 = criarAno(2026);
    }

    @AfterEach
    void limpar() {
        registros.deleteAll();
        escolas.deleteAll(escolas.findAll().stream().filter(escola -> escola.getEscolaSede() != null).toList());
        escolas.deleteAll();
        aldeias.deleteAll();
        povos.deleteAll();
        terras.deleteAll();
        municipios.deleteAll();
        anos.deleteAll();
    }

    @Test
    void deveGravarCadastroCompletoEmUmaOperacao() throws Exception {
        CadastroEscolaResponseDTO resposta = service.salvar(cadastro(null, ano2025, 10), null);

        assertNotNull(resposta.escola().id());
        assertEquals(2025, resposta.registro().anoReferencia().ano());
        assertTrue(resposta.registro().equipe().temMerendeira());
        assertEquals(3, resposta.registro().infraestrutura().numSalas());
        assertEquals(2, resposta.registro().matriculasNivel().size());
        assertEquals(1, resposta.registro().matriculasSerie().size());
        assertEquals(1, registros.count());
        assertEquals(1, equipes.count());
        assertEquals(1, infraestruturas.count());
        assertEquals(2, matriculasNivel.count());
        assertEquals(1, matriculasSerie.count());
    }

    @Test
    void deveDesfazerTudoQuandoUmaEtapaPosteriorFalhar() {
        List<CadastroEscolaRequestDTO.MatriculaNivelDTO> niveisRepetidos = List.of(
                new CadastroEscolaRequestDTO.MatriculaNivelDTO("INFANTIL", 1, 1, 2),
                new CadastroEscolaRequestDTO.MatriculaNivelDTO("INFANTIL", 2, 2, 4)
        );
        CadastroEscolaRequestDTO dto = cadastro(null, ano2025, 10, niveisRepetidos);

        assertThrows(IllegalArgumentException.class, () -> service.salvar(dto, null));

        assertEquals(0, escolas.count());
        assertEquals(0, registros.count());
        assertEquals(0, matriculasNivel.count());
    }

    @Test
    void deveDesfazerTudoQuandoUploadDaFotoLancarIOException() throws Exception {
        when(cloudinaryService.uploadImagem(any())).thenThrow(new IOException("rede indisponível"));
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(IOException.class, () -> service.salvar(cadastro(null, ano2025, 10), foto));

        assertEquals(0, escolas.count());
        assertEquals(0, registros.count());
        assertEquals(0, infraestruturas.count());
    }

    @Test
    void deveDesfazerEdicaoQuandoUploadDaFotoLancarIOException() throws Exception {
        Long escolaId = service.salvar(cadastro(null, ano2025, 10), null).escola().id();
        when(cloudinaryService.uploadImagem(any())).thenThrow(new IOException("rede indisponível"));
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(IOException.class, () -> service.editar(cadastro(escolaId, ano2025, 99), foto));

        Escola escola = escolas.findById(escolaId).orElseThrow();
        assertEquals(10, registros.findByEscolaAndAnoReferencia_Ano(escola, 2025).orElseThrow().getTotalAlunos());
    }

    @Test
    void deveDesfazerTudoQuandoEnvioDaFotoFalhar() throws Exception {
        when(cloudinaryService.uploadImagem(any())).thenThrow(new ArmazenamentoImagemNaoConfiguradoException());
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(ArmazenamentoImagemNaoConfiguradoException.class,
                () -> service.salvar(cadastro(null, ano2025, 10), foto));

        assertEquals(0, escolas.count());
        assertEquals(0, registros.count());
        assertEquals(0, infraestruturas.count());
    }

    @Test
    void deveGravarFotoEnviadaJuntoComCadastro() throws Exception {
        when(cloudinaryService.uploadImagem(any()))
                .thenReturn(Map.of("secure_url", "https://img/foto.jpg", "public_id", "sigei/foto"));
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        CadastroEscolaResponseDTO resposta = service.salvar(cadastro(null, ano2025, 10), foto);

        assertEquals("https://img/foto.jpg", resposta.registro().infraestrutura().fotoUrl());
        verify(cloudinaryService, never()).excluirImagem(any());
    }

    @Test
    void deveAtualizarAnoExistenteSemDuplicarEPreservarOutrosAnos() throws Exception {
        Long escolaId = service.salvar(cadastro(null, ano2025, 10), null).escola().id();
        service.editar(cadastro(escolaId, ano2026, 20), null);

        CadastroEscolaResponseDTO resposta = service.editar(cadastro(escolaId, ano2025, 99), null);

        assertEquals(99, resposta.registro().totalAlunos());
        assertEquals(1, escolas.count());
        assertEquals(2, registros.count());
        assertEquals(2, equipes.count());
        assertEquals(2, infraestruturas.count());
        assertEquals(4, matriculasNivel.count());
        assertEquals(2, matriculasSerie.count());
        Escola escola = escolas.findById(escolaId).orElseThrow();
        assertEquals(20, registros.findByEscolaAndAnoReferencia_Ano(escola, 2026).orElseThrow().getTotalAlunos());
    }

    @Test
    void deveRejeitarEdicaoSemIdDaEscola() {
        assertThrows(IllegalArgumentException.class, () -> service.editar(cadastro(null, ano2025, 10), null));
        assertEquals(0, escolas.count());
    }

    @Test
    void deveCadastrarAnexoVinculadoAEscolaSede() throws Exception {
        Long sedeId = service.salvar(cadastro(null, ano2025, 10), null).escola().id();
        Aldeia aldeiaAnexo = novaAldeia("Aldeia anexo");

        CadastroEscolaResponseDTO resposta = service.salvar(cadastroDaEscola(
                new EscolaRequestDTO(null, "COD-2", "Anexo cadastro", "anexo", sedeId, aldeiaAnexo.getId()),
                ano2025, 5, List.of()), null);

        assertEquals("ANEXO", resposta.escola().tipoUnidade());
        assertEquals(sedeId, resposta.escola().escolaSede().id());
        assertTrue(escolas.existsByEscolaSede_Id(sedeId));
    }

    @Test
    void deveDesfazerCadastroDeAnexoComSedeInvalida() throws Exception {
        Long sedeId = service.salvar(cadastro(null, ano2025, 10), null).escola().id();
        Long anexoId = service.salvar(cadastroDaEscola(
                new EscolaRequestDTO(null, "COD-2", "Anexo", "ANEXO", sedeId, novaAldeia("Aldeia 2").getId()),
                ano2025, 5, List.of()), null).escola().id();

        CadastroEscolaRequestDTO anexoDeAnexo = cadastroDaEscola(
                new EscolaRequestDTO(null, "COD-3", "Anexo do anexo", "ANEXO", anexoId, novaAldeia("Aldeia 3").getId()),
                ano2025, 5, List.of());

        assertThrows(IllegalArgumentException.class, () -> service.salvar(anexoDeAnexo, null));
        assertEquals(2, escolas.count());
        assertEquals(2, registros.count());
    }

    private Aldeia novaAldeia(String nome) {
        Aldeia nova = new Aldeia();
        nova.setNome(nome);
        nova.setTerraIndigena(aldeia.getTerraIndigena());
        nova.setPovo(aldeia.getPovo());
        return aldeias.save(nova);
    }

    private AnoReferencia criarAno(int valor) {
        AnoReferencia ano = new AnoReferencia();
        ano.setAno(valor);
        return anos.save(ano);
    }

    private CadastroEscolaRequestDTO cadastro(Long escolaId, AnoReferencia ano, int totalAlunos) {
        return cadastro(escolaId, ano, totalAlunos, List.of(
                new CadastroEscolaRequestDTO.MatriculaNivelDTO("TOTAL_GERAL", 5, 5, 10),
                new CadastroEscolaRequestDTO.MatriculaNivelDTO("INFANTIL", 5, 5, 10)
        ));
    }

    private CadastroEscolaRequestDTO cadastro(Long escolaId, AnoReferencia ano, int totalAlunos,
                                              List<CadastroEscolaRequestDTO.MatriculaNivelDTO> niveis) {
        return cadastroDaEscola(new EscolaRequestDTO(escolaId, "COD-1", "Escola cadastro", "SEDE", null, aldeia.getId()),
                ano, totalAlunos, niveis);
    }

    private CadastroEscolaRequestDTO cadastroDaEscola(EscolaRequestDTO escola, AnoReferencia ano, int totalAlunos,
                                              List<CadastroEscolaRequestDTO.MatriculaNivelDTO> niveis) {
        return new CadastroEscolaRequestDTO(
                escola,
                ano.getId(),
                "Estadual",
                "Prédio próprio",
                totalAlunos,
                new CadastroEscolaRequestDTO.EquipeDTO(true, false, true, false, false, true),
                new CadastroEscolaRequestDTO.InfraestruturaDTO(2010, 3, -8.1, -70.5, "ATIVA", "FLUVIAL",
                        true, "POCO", false, "SOLAR", false),
                niveis,
                List.of(new CadastroEscolaRequestDTO.MatriculaSerieDTO("1º ano", "FUNDAMENTAL", 2, 3, 5)),
                false
        );
    }
}
