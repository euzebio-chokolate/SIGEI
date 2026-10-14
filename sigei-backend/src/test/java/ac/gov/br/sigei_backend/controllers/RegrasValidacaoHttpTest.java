package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Regras que antes existiam só no frontend: senha de 8 a 72 caracteres, faixas da
 * infraestrutura (coordenadas, salas e ano de construção) e faixa do ano de
 * referência. Os IDs de relacionamento enviados não existem: sem a validação,
 * a resposta seria 404.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegrasValidacaoHttpTest {

    private static final int ANO_ATUAL = Year.now(ZoneId.of("America/Rio_Branco")).getValue();

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired Validator validator;

    private Usuario admin;

    @BeforeEach
    void preparar() {
        admin = new Usuario();
        admin.setNome("Admin regras");
        admin.setEmail("admin.regras@sigei.com");
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

    @ParameterizedTest(name = "{0} {2}")
    @CsvSource(delimiter = '|', value = {
            "/usuario/salvar        | {\"nome\":\"U\",\"email\":\"curta@sigei.com\",\"senha\":\"1234567\",\"perfil\":\"USUARIO\"} | senha",
            "/usuario/salvar        | {\"nome\":\"U\",\"email\":\"longa@sigei.com\",\"senha\":\"SENHA73\",\"perfil\":\"USUARIO\"} | senha",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"latitude\":-90.5}       | latitude",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"latitude\":91}          | latitude",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"longitude\":-181}       | longitude",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"longitude\":180.1}      | longitude",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"numSalas\":-1}          | numSalas",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"anoConstrucao\":1899}   | anoConstrucao",
            "/infraestrutura/salvar | {\"registroAnualId\":999999,\"anoConstrucao\":PROXIMO} | anoConstrucao",
            "/anos/salvar           | {\"ano\":1899}                                       | ano",
            "/anos/salvar           | {\"ano\":DEPOIS_DO_PROXIMO}                          | ano",
            "/anos/salvar           | {}                                                   | ano"
    })
    void valorForaDaRegraDeveResponder400(String rota, String corpo, String campo) throws Exception {
        mvc.perform(post(rota)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo
                                .replace("SENHA73", "a".repeat(73))
                                .replace("DEPOIS_DO_PROXIMO", String.valueOf(ANO_ATUAL + 2))
                                .replace("PROXIMO", String.valueOf(ANO_ATUAL + 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + campo).isArray());
    }

    @Test
    void edicaoDeUsuarioComSenhaVaziaDeveManterASenha() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Sem troca de senha");
        usuario.setEmail("sem.troca@sigei.com");
        usuario.setSenha("hash-original");
        usuario.setPerfil(PerfilUsuario.USUARIO);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        usuario = usuarios.save(usuario);

        try {
            // O formulário envia senha vazia quando ela não é alterada
            mvc.perform(put("/usuario/editar")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":" + usuario.getId() + ",\"nome\":\"Renomeado\",\"email\":\"sem.troca@sigei.com\","
                                    + "\"senha\":\"\",\"perfil\":\"USUARIO\",\"ativo\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Renomeado"));

            assertEquals("hash-original", usuarios.findById(usuario.getId()).orElseThrow().getSenha());
        } finally {
            usuarios.deleteById(usuario.getId());
        }
    }

    @Test
    void limitesDasFaixasDevemSerAceitos() {
        assertTrue(validator.validate(new InfraestruturaEscolaRequestDTO(null, 1L, 1900, 0, -90.0, -180.0,
                null, null, null, null, null, null, null)).isEmpty());
        assertTrue(validator.validate(new InfraestruturaEscolaRequestDTO(null, 1L, ANO_ATUAL, 12, 90.0, 180.0,
                null, null, null, null, null, null, null)).isEmpty());

        assertTrue(validator.validate(new AnoReferenciaRequestDTO(null, 1900)).isEmpty());
        // A abertura de ano e a importação criam o ano seguinte; ele precisa continuar editável
        assertTrue(validator.validate(new AnoReferenciaRequestDTO(null, ANO_ATUAL + 1)).isEmpty());
    }

    @Test
    void cadastroCompletoDeveValidarAInfraestrutura() {
        var infraestrutura = new CadastroEscolaRequestDTO.InfraestruturaDTO(ANO_ATUAL + 1, -1, 95.0, -200.0,
                null, null, null, null, null, null, null);

        Set<ConstraintViolation<CadastroEscolaRequestDTO.InfraestruturaDTO>> violacoes = validator.validate(infraestrutura);

        assertEquals(Set.of("anoConstrucao", "numSalas", "latitude", "longitude"),
                violacoes.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet()));
    }
}
