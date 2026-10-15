package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Conflitos respondem 409 com uma mensagem própria do service, e não com a
 * violação de constraint do banco: exclusões de registros com vínculos e
 * cadastros duplicados. Cada teste é desfeito ao final (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConflitoHttpTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired MunicipioRepository municipios;
    @Autowired TerraIndigenaRepository terras;
    @Autowired PovoRepository povos;
    @Autowired AldeiaRepository aldeias;
    @Autowired EscolaRepository escolas;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;
    @Autowired EquipeEscolaRepository equipes;

    private String token;
    private Municipio municipio;
    private TerraIndigena terra;
    private Povo povo;
    private Aldeia aldeia;

    @BeforeEach
    void preparar() {
        Usuario admin = new Usuario();
        admin.setNome("Admin conflito");
        admin.setEmail("admin.conflito@sigei.com");
        admin.setSenha("hash-irrelevante");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAlterarSenha(false);
        token = "Bearer " + jwtService.gerarToken(usuarios.save(admin));

        municipio = new Municipio();
        municipio.setNome("Município conflito");
        municipios.save(municipio);
        terra = new TerraIndigena();
        terra.setNome("Terra conflito");
        terra.setMunicipio(municipio);
        terras.save(terra);
        povo = new Povo();
        povo.setNome("Povo conflito");
        povos.save(povo);
        aldeia = aldeia("Aldeia conflito");
    }

    @Test
    void excluirPovoComAldeiasExplicaOVinculo() throws Exception {
        aldeia("Outra aldeia conflito");

        excluir("/povo/excluir/" + povo.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Não é possível excluir o povo: ele tem 2 aldeias vinculadas."));
    }

    @Test
    void excluirTerraComAldeiaExplicaOVinculo() throws Exception {
        excluir("/terra/excluir/" + terra.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Não é possível excluir a terra indígena: ela tem 1 aldeia vinculada."));
    }

    @Test
    void excluirMunicipioComTerraExplicaOVinculo() throws Exception {
        excluir("/municipio/excluir/" + municipio.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "Não é possível excluir o município: ele tem 1 terra indígena vinculada."));
    }

    @Test
    void excluirAldeiaComEscolasExplicaOVinculo() throws Exception {
        escola("SEDE", null, aldeia);
        escola("SEDE", null, aldeia);

        excluir("/aldeia/excluir/" + aldeia.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Não é possível excluir a aldeia: ela tem 2 escolas vinculadas."));
    }

    @Test
    void excluirEscolaComAnexosExplicaOVinculo() throws Exception {
        Escola sede = escola("SEDE", null, aldeia);
        escola("ANEXO", sede, aldeia("Aldeia do anexo conflito"));

        excluir("/escola/excluir/" + sede.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Não é possível excluir a escola: ela tem 1 escola anexa vinculada."));
    }

    @Test
    void excluirAnoComRegistrosExplicaOVinculo() throws Exception {
        AnoReferencia ano = ano(1951);
        registro(escola("SEDE", null, aldeia), ano);

        excluir("/anos/excluir/" + ano.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Não é possível excluir o ano 1951: ele tem 1 registro anual de escola."));
    }

    @Test
    void excluirPovoSemVinculosContinuaPermitido() throws Exception {
        Povo livre = new Povo();
        livre.setNome("Povo livre conflito");
        povos.save(livre);

        excluir("/povo/excluir/" + livre.getId()).andExpect(status().is2xxSuccessful());
    }

    @Test
    void anoDuplicadoRespondeMensagemPropria() throws Exception {
        ano(1952);

        enviar(post("/anos/salvar"), "{\"ano\":1952}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("O ano 1952 já está cadastrado."));
    }

    @Test
    void aldeiaPodeTerMaisDeUmaEscola() throws Exception {
        // Como na aldeia Belo Monte (Feijó), que tem duas escolas-sede
        escola("SEDE", null, aldeia);

        enviar(post("/escola/salvar"),
                "{\"codigo\":\"COD-NOVA\",\"nome\":\"Escola nova\",\"tipoUnidade\":\"SEDE\",\"aldeiaId\":" + aldeia.getId() + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aldeia.id").value(aldeia.getId()));
    }

    @Test
    void segundaEquipeDoRegistroRespondeMensagemPropria() throws Exception {
        RegistroAnual registro = registro(escola("SEDE", null, aldeia), ano(1953));
        EquipeEscola equipe = new EquipeEscola();
        equipe.setRegistroAnual(registro);
        equipes.save(equipe);

        enviar(post("/equipe/salvar"), "{\"registroAnualId\":" + registro.getId()
                + ",\"temProfessorResponsavel\":true,\"temEquipeGestora\":false,\"temMerendeira\":false,"
                + "\"temZeladora\":false,\"temBarqueiro\":false,\"necessitaTransporte\":false}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Este registro anual já tem uma equipe cadastrada; edite a existente."));
    }

    @Test
    void usuarioComEmailRepetidoRespondeMensagemPropria() throws Exception {
        enviar(post("/usuario/salvar"),
                "{\"nome\":\"Outro\",\"email\":\"ADMIN.conflito@sigei.com\",\"senha\":\"12345678\",\"perfil\":\"USUARIO\",\"ativo\":true}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um usuário cadastrado com esse e-mail"));
    }

    private ResultActions excluir(String rota) throws Exception {
        return mvc.perform(delete(rota).header(HttpHeaders.AUTHORIZATION, token));
    }

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
                                 String corpo) throws Exception {
        return mvc.perform(requisicao
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private Aldeia aldeia(String nome) {
        Aldeia nova = new Aldeia();
        nova.setNome(nome);
        nova.setTerraIndigena(terra);
        nova.setPovo(povo);
        return aldeias.save(nova);
    }

    private Escola escola(String tipo, Escola sede, Aldeia aldeiaDaEscola) {
        Escola escola = new Escola();
        escola.setCodigo("COD-" + aldeiaDaEscola.getNome());
        escola.setNome("Escola " + aldeiaDaEscola.getNome());
        escola.setTipoUnidade(tipo);
        escola.setEscolaSede(sede);
        escola.setAldeia(aldeiaDaEscola);
        return escolas.save(escola);
    }

    private AnoReferencia ano(int valor) {
        AnoReferencia ano = new AnoReferencia();
        ano.setAno(valor);
        return anos.save(ano);
    }

    private RegistroAnual registro(Escola escola, AnoReferencia ano) {
        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(ano);
        return registros.save(registro);
    }
}
