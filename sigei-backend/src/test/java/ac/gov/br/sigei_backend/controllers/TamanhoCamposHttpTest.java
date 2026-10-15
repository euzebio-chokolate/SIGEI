package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Textos maiores que as colunas varchar(255) resultam em 400 com o mapa de campos,
 * em vez de chegar ao banco e voltar como erro de integridade (409). Os IDs de
 * relacionamento enviados não existem: sem a validação, a resposta seria outra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TamanhoCamposHttpTest {

    private static final String LONGO = "a".repeat(256);

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired Validator validator;

    private Usuario admin;

    @BeforeEach
    void preparar() {
        admin = new Usuario();
        admin.setNome("Admin tamanho");
        admin.setEmail("admin.tamanho@sigei.com");
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
            "/aldeia/salvar         | {\"nome\":\"LONGO\",\"terraIndigenaId\":999999,\"povoId\":999999}                       | nome",
            "/escola/salvar         | {\"codigo\":\"LONGO\",\"nome\":\"E\",\"tipoUnidade\":\"SEDE\",\"aldeiaId\":999999}       | codigo",
            "/escola/salvar         | {\"codigo\":\"1\",\"nome\":\"LONGO\",\"tipoUnidade\":\"SEDE\",\"aldeiaId\":999999}       | nome",
            "/matriculaNivel/salvar | {\"registroId\":999999,\"nivel\":\"LONGO\"}                                             | nivel",
            "/matriculaSerie/salvar | {\"registroId\":999999,\"denominacao\":\"LONGO\"}                                       | denominacao",
            "/matriculaSerie/salvar | {\"registroId\":999999,\"denominacao\":\"1º ano\",\"etapaEnsino\":\"LONGO\"}             | etapaEnsino",
            "/municipio/salvar      | {\"nome\":\"LONGO\"}                                                                    | nome",
            "/povo/salvar           | {\"nome\":\"LONGO\"}                                                                    | nome",
            "/terra/salvar          | {\"nome\":\"LONGO\",\"municipioId\":999999}                                             | nome",
            "/registroAnual/salvar  | {\"escolaId\":999999,\"anoReferenciaId\":999999,\"rede\":\"LONGO\",\"localFuncionamento\":\"Aldeia\"} | rede",
            "/registroAnual/salvar  | {\"escolaId\":999999,\"anoReferenciaId\":999999,\"rede\":\"Estadual\",\"localFuncionamento\":\"LONGO\"} | localFuncionamento",
            "/usuario/salvar        | {\"nome\":\"LONGO\",\"email\":\"longo@sigei.com\",\"senha\":\"senha-forte-123\",\"perfil\":\"USUARIO\"} | nome",
            "/usuario/salvar        | {\"nome\":\"Longo\",\"email\":\"LONGO@sigei.com\",\"senha\":\"senha-forte-123\",\"perfil\":\"USUARIO\"} | email"
    })
    void textoMaiorQueAColunaDeveResponder400(String rota, String corpo, String campo) throws Exception {
        mvc.perform(post(rota)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.replace("LONGO", LONGO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + campo).isArray());
    }

    @Test
    void cadastroCompletoDeveLimitarOsTextosDoRegistroEDasMatriculas() {
        var cadastro = new CadastroEscolaRequestDTO(
                new EscolaRequestDTO(null, "1", LONGO, "SEDE", null, 1L),
                1L, LONGO, LONGO, 0,
                new CadastroEscolaRequestDTO.EquipeDTO(false, false, false, false, false, false),
                new CadastroEscolaRequestDTO.InfraestruturaDTO(null, null, null, null, null, null, null, null, null, null, null),
                List.of(new CadastroEscolaRequestDTO.MatriculaNivelDTO(LONGO, 0, 0, 0)),
                List.of(new CadastroEscolaRequestDTO.MatriculaSerieDTO(LONGO, LONGO, 0, 0, 0)),
                false);

        Set<ConstraintViolation<CadastroEscolaRequestDTO>> violacoes = validator.validate(cadastro);

        assertEquals(Set.of("escola.nome", "rede", "localFuncionamento", "matriculasNivel[0].nivel",
                        "matriculasSerie[0].denominacao", "matriculasSerie[0].etapaEnsino"),
                violacoes.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet()));
    }
}
