package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pré-visualização das importações (simulacao=true): devolve o resultado da
 * gravação sem gravar nada. Usa transações reais, sem @Transactional no teste,
 * porque é o rollback da simulação que está sendo verificado; os dados da
 * importação real usada na preparação são removidos ao final.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoSimulacaoHttpTest {

    private static final int ANO = 2001;
    private static final String CODIGO = "12999901";
    private static final String TERRA = "TERRA SIMULACAO";
    private static final String POVO = "POVO SIMULACAO";
    private static final String ALDEIA = "ALDEIA SIMULACAO";

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
    @Autowired MatriculaNivelRepository niveis;

    private Usuario admin;
    private String token;
    private final List<Municipio> municipiosCriados = new ArrayList<>();

    @BeforeEach
    void preparar() {
        // A importação de escolas exige estes municípios; outros testes podem tê-los removido
        for (String nome : List.of("Feijó", "Marechal Thaumaturgo", "Santa Rosa do Purus")) {
            if (municipio(nome).isEmpty()) {
                Municipio municipio = new Municipio();
                municipio.setNome(nome);
                municipiosCriados.add(municipios.save(municipio));
            }
        }

        admin = new Usuario();
        admin.setNome("Admin simulação");
        admin.setEmail("admin.simulacao@sigei.com");
        admin.setSenha("hash-irrelevante");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAlterarSenha(false);
        admin = usuarios.save(admin);
        token = "Bearer " + jwtService.gerarToken(admin);
    }

    @AfterEach
    void limpar() {
        usuarios.delete(admin);
        escolas.deleteAll(escolas.findAllByCodigo(CODIGO));
        municipio("Feijó").flatMap(feijo -> terras.findByNomeIgnoreCaseAndMunicipio(TERRA, feijo)).ifPresent(terra -> {
            povos.findByNomeIgnoreCase(POVO).flatMap(povo ->
                    aldeias.findByNomeIgnoreCaseAndTerraIndigenaAndPovo(ALDEIA, terra, povo)).ifPresent(aldeias::delete);
            terras.delete(terra);
        });
        povos.findByNomeIgnoreCase(POVO).ifPresent(povos::delete);
        anos.findByAno(ANO).ifPresent(anos::delete);
        municipios.deleteAll(municipiosCriados);
        municipiosCriados.clear();
    }

    @Test
    void simularEscolasMostraOResultadoSemGravar() throws Exception {
        importar("infraestrutura", planilhaEscolas(), true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolasCriadas").value(1))
                .andExpect(jsonPath("$.registrosAnuaisCriados").value(1))
                .andExpect(jsonPath("$.aldeiasCriadas").value(1))
                .andExpect(jsonPath("$.povosCriados").value(1))
                .andExpect(jsonPath("$.erros").isEmpty());

        assertTrue(escolas.findAllByCodigo(CODIGO).isEmpty());
        assertTrue(povos.findByNomeIgnoreCase(POVO).isEmpty());
        // O ano que a importação criaria também é desfeito
        assertTrue(anos.findByAno(ANO).isEmpty());
    }

    @Test
    void confirmarDepoisDaSimulacaoGravaOMesmoResultado() throws Exception {
        importar("infraestrutura", planilhaEscolas(), true).andExpect(status().isOk());

        importar("infraestrutura", planilhaEscolas(), false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolasCriadas").value(1))
                .andExpect(jsonPath("$.registrosAnuaisCriados").value(1));

        assertEquals(1, escolas.findAllByCodigo(CODIGO).size());
        assertTrue(anos.findByAno(ANO).isPresent());
    }

    @Test
    void simularMatriculasNaoAlteraORegistro() throws Exception {
        importar("infraestrutura", planilhaEscolas(), false).andExpect(status().isOk());

        importar("matriculas", planilhaMatriculas(), true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrosAnuaisAtualizados").value(1))
                .andExpect(jsonPath("$.erros").isEmpty());

        RegistroAnual registro = registros.findByEscolaAndAnoReferencia_Ano(
                escolas.findAllByCodigo(CODIGO).getFirst(), ANO).orElseThrow();
        assertEquals(30, registro.getTotalAlunos());
        assertTrue(niveis.findByRegistro(registro).isEmpty());
    }

    @Test
    void erroDaPlanilhaInteiraNaSimulacaoResponde400() throws Exception {
        importar("matriculas", planilha(new String[]{"CÓDIGO"}, new String[]{CODIGO}), true)
                .andExpect(status().isBadRequest());

        assertTrue(anos.findByAno(ANO).isEmpty());
    }

    private ResultActions importar(String tipo, byte[] planilha, boolean simulacao) throws Exception {
        return mvc.perform(multipart("/importacoes/" + tipo)
                .file(new MockMultipartFile("arquivo", "planilha.xlsx", null, planilha))
                .param("ano", String.valueOf(ANO))
                .param("simulacao", String.valueOf(simulacao))
                .header(HttpHeaders.AUTHORIZATION, token));
    }

    private Optional<Municipio> municipio(String nome) {
        return municipios.findAll().stream()
                .filter(municipio -> municipio.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    private static byte[] planilhaEscolas() throws IOException {
        return planilha(
                new String[]{"MUNICÍPIO", "CÓDIGO", "NOME DA ESCOLA", "TERRA INDÍGENA", "ALDEIA", "POVO", "Nº DE ALUNOS"},
                new String[]{"FEIJÓ", CODIGO, "ESC IND SIMULACAO", TERRA, ALDEIA, POVO, "30"});
    }

    private static byte[] planilhaMatriculas() throws IOException {
        List<String> titulos = List.of("CÓDIGO", "NOME DA ESCOLA", "ALDEIA", "M", "F", "ENSINO INFANTIL",
                "1º ANO", "2º ANO", "3º ANO", "4º ANO", "5º ANO", "6º ANO", "7º ANO", "8º ANO", "9º ANO",
                "1ª SÉRIE", "2ª SÉRIE", "3ª SÉRIE");
        String[] valores = new String[titulos.size()];
        valores[0] = CODIGO;
        valores[1] = "ESC IND SIMULACAO";
        valores[2] = ALDEIA;
        // 13 etapas com 2 alunos: M + F precisa ser igual à soma das etapas
        valores[3] = "13";
        valores[4] = "13";
        for (int indice = 5; indice < valores.length; indice++) {
            valores[indice] = "2";
        }
        return planilha(titulos.toArray(String[]::new), valores);
    }

    private static byte[] planilha(String[]... linhas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Sheet aba = workbook.createSheet("Dados");
            for (int indice = 0; indice < linhas.length; indice++) {
                Row linha = aba.createRow(indice);
                for (int coluna = 0; coluna < linhas[indice].length; coluna++) {
                    linha.createCell(coluna).setCellValue(linhas[indice][coluna]);
                }
            }
            workbook.write(saida);
            return saida.toByteArray();
        }
    }
}
