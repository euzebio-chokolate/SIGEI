package ac.gov.br.sigei_backend.config.criptografia;

import ac.gov.br.sigei_backend.models.ChaveDados;
import ac.gov.br.sigei_backend.models.Professor;
import ac.gov.br.sigei_backend.repository.ChaveDadosRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cifra em envelope: a chave de dados fica no banco, cifrada pela chave mestra e
 * pela chave de recuperação. Cada teste monta o gerenciador com outra
 * configuração, como numa nova inicialização da aplicação.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class GerenciadorChaveDadosTest {

    /** A mesma chave de application-test.properties, usada pelo conversor nesta fatia. */
    private static final String CHAVE_TESTE = "c2lnZWktY2hhdmUtZGUtdGVzdGUtZGFkb3Mtc2Vuc2l2ZWlzLTMyYg==";
    private static final String OUTRA_MESTRA = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String TERCEIRA_MESTRA = Base64.getEncoder().encodeToString("x".repeat(32).getBytes());

    private static KeyPair recuperacao;

    @Autowired ChaveDadosRepository repository;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    @BeforeAll
    static void gerarChaveDeRecuperacao() throws NoSuchAlgorithmException {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(3072);
        recuperacao = gerador.generateKeyPair();
    }

    @Test
    void primeiraInicializacaoUsaAChaveMestraComoChaveDeDados() {
        byte[] chave = gerenciador(CHAVE_TESTE, null, null, null).chaveDados();

        // Os valores já gravados com a chave mestra continuam legíveis
        String gravadoAntes = new CriptografiaDadosSensiveis(CHAVE_TESTE).cifrar("52998224725");
        assertEquals("52998224725", new CriptografiaDadosSensiveis(chave).decifrar(gravadoAntes));

        ChaveDados registro = repository.findById(ChaveDados.ID).orElseThrow();
        assertTrue(registro.getCifradaMestra().startsWith("v1:"));
        assertFalse(registro.getCifradaMestra().contains(CHAVE_TESTE));
        assertNull(registro.getCifradaRecuperacao());
    }

    @Test
    void trocaDaChaveMestraNaoMudaAChaveDeDados() {
        byte[] original = gerenciador(CHAVE_TESTE, null, null, null).chaveDados();

        byte[] depoisDaTroca = gerenciador(OUTRA_MESTRA, CHAVE_TESTE, null, null).chaveDados();
        assertArrayEquals(original, depoisDaTroca);

        // Concluída a troca, a anterior não é mais necessária
        assertArrayEquals(original, gerenciador(OUTRA_MESTRA, null, null, null).chaveDados());
        assertThrows(IllegalStateException.class, () -> gerenciador(CHAVE_TESTE, null, null, null).chaveDados());
    }

    @Test
    void chaveMestraErradaImpedeAInicializacao() {
        gerenciador(CHAVE_TESTE, null, null, null).chaveDados();

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> gerenciador(OUTRA_MESTRA, TERCEIRA_MESTRA, null, null).chaveDados());
        assertTrue(erro.getMessage().contains("SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR"));
        assertFalse(erro.getMessage().contains(OUTRA_MESTRA));
    }

    @Test
    void chavePrivadaDeRecuperacaoRecuperaOsDadosAposPerderAMestra() {
        String publica = Base64.getEncoder().encodeToString(recuperacao.getPublic().getEncoded());
        String privada = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder().encodeToString(recuperacao.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";

        byte[] original = gerenciador(CHAVE_TESTE, null, publica, null).chaveDados();
        assertNotNull(repository.findById(ChaveDados.ID).orElseThrow().getCifradaRecuperacao());

        // A chave mestra se perdeu: uma nova, com a chave privada, recupera a chave de dados
        assertArrayEquals(original, gerenciador(OUTRA_MESTRA, null, publica, privada).chaveDados());

        // E a nova chave mestra passa a abrir sozinha
        assertArrayEquals(original, gerenciador(OUTRA_MESTRA, null, publica, null).chaveDados());
    }

    @Test
    void chavePublicaFracaERecusada() throws NoSuchAlgorithmException {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        String fraca = Base64.getEncoder().encodeToString(gerador.generateKeyPair().getPublic().getEncoded());

        assertThrows(IllegalStateException.class, () -> gerenciador(CHAVE_TESTE, null, fraca, null).chaveDados());
    }

    @Test
    void dadosGravadosComOutraChaveImpedemAInicializacao() {
        // O conversor desta fatia grava com CHAVE_TESTE
        Professor professor = new Professor();
        professor.setNome("Professor da amostra");
        professor.setCpf("52998224725");
        entityManager.persist(professor);
        entityManager.flush();

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> gerenciador(OUTRA_MESTRA, null, null, null).chaveDados());
        assertTrue(erro.getMessage().startsWith("A chave de dados não decifra"));
        assertTrue(repository.findById(ChaveDados.ID).isEmpty());

        // Com a chave certa, sobe normalmente
        assertDoesNotThrow(() -> gerenciador(CHAVE_TESTE, null, null, null).chaveDados());
    }

    private GerenciadorChaveDados gerenciador(String mestra, String anterior, String publica, String privada) {
        return new GerenciadorChaveDados(repository, entityManager, transactionManager, mestra, anterior, publica, privada);
    }
}
