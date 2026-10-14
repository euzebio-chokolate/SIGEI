package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * As edições validam o corpo como os cadastros: campo obrigatório vazio resulta em
 * 400 com o mapa de campos, antes de o service procurar o registro (o ID enviado
 * não existe, e sem a validação a resposta seria 404).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EdicaoValidacaoHttpTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;

    private Usuario admin;

    @BeforeEach
    void preparar() {
        admin = new Usuario();
        admin.setNome("Admin edição");
        admin.setEmail("admin.edicao@sigei.com");
        admin.setSenha("hash-irrelevante");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAlterarSenha(false);
        admin = usuarios.save(admin);
    }

    @AfterEach
    void limpar() {
        usuarios.deleteById(admin.getId());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "/aldeia/editar         | {\"id\":999999,\"nome\":\"\",\"terraIndigenaId\":1,\"povoId\":1}   | nome",
            "/aldeia/editar         | {\"id\":999999,\"nome\":\"Aldeia\",\"povoId\":1}                   | terraIndigenaId",
            "/escola/editar         | {\"id\":999999,\"codigo\":\"1\",\"nome\":\" \",\"tipoUnidade\":\"SEDE\",\"aldeiaId\":1} | nome",
            "/infraestrutura/editar | {\"id\":999999}                                                    | registroAnualId",
            "/matriculaNivel/editar | {\"id\":999999,\"registroId\":1,\"nivel\":\"\"}                     | nivel",
            "/municipio/editar      | {\"id\":999999,\"nome\":\"\"}                                      | nome",
            "/povo/editar           | {\"id\":999999,\"nome\":\"\"}                                      | nome",
            "/terra/editar          | {\"id\":999999,\"nome\":\"Terra\"}                                 | municipioId"
    })
    void edicaoComCampoObrigatorioInvalidoDeveResponder400(String rota, String corpo, String campo) throws Exception {
        mvc.perform(put(rota)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + campo).isArray());
    }
}
