package ac.gov.br.sigei_backend.config.criptografia;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Operações da cifra em envelope: a chave de dados é guardada cifrada pela chave
 * mestra (AES-256-GCM, com chave derivada da mestra) e pela chave pública de
 * recuperação (RSA-OAEP com SHA-256). Nenhuma mensagem de erro inclui chaves.
 */
final class EnvelopeChave {

    private static final String PREFIXO = "v1:";
    private static final String FINALIDADE_ENVELOPE = "sigei-envelope-chave-dados";
    private static final byte[] DADOS_ASSOCIADOS = "sigei-chave-dados".getBytes(StandardCharsets.UTF_8);
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG_BITS = 128;
    private static final int TAMANHO_MINIMO_CHAVE = 32;
    private static final int TAMANHO_MINIMO_RSA = 3072;
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private EnvelopeChave() {
    }

    /** Chave mestra em Base64, com pelo menos 32 bytes. */
    static byte[] decodificarChaveMestra(String base64, String variavel) {
        String mensagem = "Defina " + variavel + " com pelo menos 32 bytes aleatórios em Base64 " +
                "(gere com: openssl rand -base64 32).";

        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException(mensagem);
        }

        byte[] chave;
        try {
            chave = Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(mensagem, e);
        }

        if (chave.length < TAMANHO_MINIMO_CHAVE) {
            throw new IllegalStateException(mensagem);
        }
        return chave;
    }

    static String cifrarComMestra(byte[] chaveMestra, byte[] chaveDados) {
        try {
            byte[] iv = new byte[TAMANHO_IV];
            ALEATORIO.nextBytes(iv);

            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.ENCRYPT_MODE, chaveEnvelope(chaveMestra), new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            cifra.updateAAD(DADOS_ASSOCIADOS);
            byte[] cifrado = cifra.doFinal(chaveDados);

            return PREFIXO + Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array());

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível cifrar a chave de dados.", e);
        }
    }

    /** Vazio quando a chave mestra não é a que cifrou o registro. */
    static Optional<byte[]> decifrarComMestra(byte[] chaveMestra, String cifrado) {
        if (cifrado == null || !cifrado.startsWith(PREFIXO)) {
            return Optional.empty();
        }

        try {
            byte[] dados = Base64.getDecoder().decode(cifrado.substring(PREFIXO.length()));

            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.DECRYPT_MODE, chaveEnvelope(chaveMestra),
                    new GCMParameterSpec(TAMANHO_TAG_BITS, dados, 0, TAMANHO_IV));
            cifra.updateAAD(DADOS_ASSOCIADOS);
            return Optional.of(cifra.doFinal(dados, TAMANHO_IV, dados.length - TAMANHO_IV));

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static String cifrarParaRecuperacao(PublicKey chavePublica, byte[] chaveDados) {
        try {
            Cipher cifra = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cifra.init(Cipher.ENCRYPT_MODE, chavePublica, OAEP);
            return PREFIXO + Base64.getEncoder().encodeToString(cifra.doFinal(chaveDados));

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível cifrar a chave de dados para recuperação.", e);
        }
    }

    /** Vazio quando a chave privada não corresponde à pública usada na cifra. */
    static Optional<byte[]> decifrarComRecuperacao(PrivateKey chavePrivada, String cifrado) {
        if (cifrado == null || !cifrado.startsWith(PREFIXO)) {
            return Optional.empty();
        }

        try {
            Cipher cifra = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cifra.init(Cipher.DECRYPT_MODE, chavePrivada, OAEP);
            return Optional.of(cifra.doFinal(Base64.getDecoder().decode(cifrado.substring(PREFIXO.length()))));

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Chave pública RSA em PEM ou em Base64 do DER (X.509), com pelo menos 3072 bits. */
    static PublicKey lerChavePublica(String texto, String variavel) {
        try {
            PublicKey chave = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der(texto)));

            if (((RSAPublicKey) chave).getModulus().bitLength() < TAMANHO_MINIMO_RSA) {
                throw new IllegalStateException(variavel + " deve ser uma chave RSA de pelo menos 3072 bits.");
            }
            return chave;

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException(variavel + " não é uma chave pública RSA válida (PEM ou Base64).", e);
        }
    }

    /** Chave privada RSA em PEM ou em Base64 do DER (PKCS#8, sem senha). */
    static PrivateKey lerChavePrivada(String texto, String variavel) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der(texto)));

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException(variavel + " não é uma chave privada RSA válida (PKCS#8, PEM ou Base64).", e);
        }
    }

    /** SHA-256 da chave pública, para saber se a cópia de recuperação está atualizada. */
    static String impressao(PublicKey chavePublica) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(chavePublica.getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível.", e);
        }
    }

    private static SecretKeySpec chaveEnvelope(byte[] chaveMestra) {
        return new SecretKeySpec(CriptografiaDadosSensiveis.derivar(chaveMestra, FINALIDADE_ENVELOPE), "AES");
    }

    /** Aceita PEM (com cabeçalho) ou só o Base64 do DER, com ou sem quebras de linha. */
    private static byte[] der(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Chave vazia.");
        }
        String base64 = texto
                .replaceAll("-----(BEGIN|END) [A-Z ]+-----", "")
                .replace("\\n", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
