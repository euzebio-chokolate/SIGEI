package ac.gov.br.sigei_backend.config.criptografia;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CriptografiaDadosSensiveisTest {

    private static final String CHAVE = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OUTRA_CHAVE = Base64.getEncoder().encodeToString("outra-chave-com-32-bytes-ou-mais".getBytes());

    private final CriptografiaDadosSensiveis criptografia = new CriptografiaDadosSensiveis(CHAVE);

    @Test
    void deveCifrarEDecifrar() {
        String cifrado = criptografia.cifrar("52998224725");

        assertTrue(cifrado.startsWith("v1:"));
        assertFalse(cifrado.contains("52998224725"));
        assertEquals("52998224725", criptografia.decifrar(cifrado));
    }

    @Test
    void mesmoValorDeveGerarTextosCifradosDiferentes() {
        assertNotEquals(criptografia.cifrar("12345"), criptografia.cifrar("12345"));
    }

    @Test
    void nuloDevePermanecerNulo() {
        assertNull(criptografia.cifrar(null));
        assertNull(criptografia.decifrar(null));
        assertNull(criptografia.indice(null));
    }

    @Test
    void indiceDeveSerDeterministicoEDependerDaChave() {
        String indice = criptografia.indice("52998224725");

        assertEquals(64, indice.length());
        assertEquals(indice, criptografia.indice("52998224725"));
        assertNotEquals(indice, criptografia.indice("11144477735"));
        assertNotEquals(indice, new CriptografiaDadosSensiveis(OUTRA_CHAVE).indice("52998224725"));
    }

    @Test
    void valorAdulteradoOuDeOutraChaveNaoDeveSerDecifrado() {
        String cifrado = criptografia.cifrar("52998224725");
        char ultimo = cifrado.charAt(cifrado.length() - 2);
        String adulterado = cifrado.substring(0, cifrado.length() - 2) + (ultimo == 'A' ? 'B' : 'A') + cifrado.charAt(cifrado.length() - 1);

        assertThrows(IllegalStateException.class, () -> criptografia.decifrar(adulterado));
        assertThrows(IllegalStateException.class, () -> new CriptografiaDadosSensiveis(OUTRA_CHAVE).decifrar(cifrado));
        assertThrows(IllegalStateException.class, () -> criptografia.decifrar("52998224725"));
    }

    @Test
    void chaveAusenteOuCurtaDeveImpedirAInicializacao() {
        assertThrows(IllegalStateException.class, () -> new CriptografiaDadosSensiveis(""));
        assertThrows(IllegalStateException.class, () -> new CriptografiaDadosSensiveis("nao-e-base64!"));
        assertThrows(IllegalStateException.class,
                () -> new CriptografiaDadosSensiveis(Base64.getEncoder().encodeToString(new byte[16])));
    }
}
