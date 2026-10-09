package ac.gov.br.sigei_backend.config.criptografia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Cifra os dados sensíveis dos professores (CPF, matrícula e número do contrato)
 * antes de gravá-los no banco, inclusive nas tabelas de auditoria do Envers.
 *
 * <p>Usa AES-256-GCM com IV aleatório: o mesmo valor gera textos cifrados
 * diferentes, e qualquer alteração no banco é detectada na leitura. O valor
 * gravado tem o formato {@code v1:<Base64(IV + texto cifrado + tag)>}; o prefixo
 * identifica a versão do formato, para uma futura troca de chave.
 *
 * <p>Como o texto cifrado não permite busca nem unicidade, o CPF também é gravado
 * como índice cego ({@link #indice(String)}): um HMAC-SHA256 determinístico, que só
 * pode ser calculado com a chave.
 *
 * <p>As chaves de cifra e de índice são derivadas da chave mestra
 * {@code SIGEI_CHAVE_DADOS_SENSIVEIS}. Perder essa chave torna os dados
 * irrecuperáveis; trocá-la exige recifrar os registros existentes.
 */
@Component
public class CriptografiaDadosSensiveis {

    private static final String PREFIXO = "v1:";
    private static final String ALGORITMO_CIFRA = "AES/GCM/NoPadding";
    private static final String ALGORITMO_HMAC = "HmacSHA256";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG_BITS = 128;
    private static final int TAMANHO_MINIMO_CHAVE = 32;

    private final SecretKey chaveCifra;
    private final SecretKey chaveIndice;
    private final SecureRandom aleatorio = new SecureRandom();

    public CriptografiaDadosSensiveis(
            @Value("${sigei.dados-sensiveis.chave:}") String chaveBase64
    ) {
        byte[] chaveMestra = decodificarChave(chaveBase64);

        this.chaveCifra = new SecretKeySpec(
                derivar(chaveMestra, "sigei-dados-sensiveis-cifra"),
                "AES"
        );
        this.chaveIndice = new SecretKeySpec(
                derivar(chaveMestra, "sigei-dados-sensiveis-indice"),
                ALGORITMO_HMAC
        );
    }

    public String cifrar(String texto) {
        if (texto == null) {
            return null;
        }

        try {
            byte[] iv = new byte[TAMANHO_IV];
            aleatorio.nextBytes(iv);

            Cipher cifra = Cipher.getInstance(ALGORITMO_CIFRA);
            cifra.init(Cipher.ENCRYPT_MODE, chaveCifra, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            byte[] cifrado = cifra.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = ByteBuffer.allocate(iv.length + cifrado.length)
                    .put(iv)
                    .put(cifrado)
                    .array();

            return PREFIXO + Base64.getEncoder().encodeToString(resultado);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível cifrar o dado sensível.", e);
        }
    }

    public String decifrar(String valor) {
        if (valor == null) {
            return null;
        }

        if (!valor.startsWith(PREFIXO)) {
            throw new IllegalStateException("Dado sensível gravado em formato desconhecido.");
        }

        try {
            byte[] dados = Base64.getDecoder().decode(valor.substring(PREFIXO.length()));

            Cipher cifra = Cipher.getInstance(ALGORITMO_CIFRA);
            cifra.init(Cipher.DECRYPT_MODE, chaveCifra, new GCMParameterSpec(TAMANHO_TAG_BITS, dados, 0, TAMANHO_IV));
            byte[] texto = cifra.doFinal(dados, TAMANHO_IV, dados.length - TAMANHO_IV);

            return new String(texto, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // Não inclui o valor na mensagem: ela pode chegar aos logs
            throw new IllegalStateException(
                    "Não foi possível decifrar o dado sensível. Confira SIGEI_CHAVE_DADOS_SENSIVEIS.",
                    e
            );
        }
    }

    /** Índice cego: HMAC-SHA256 em hexadecimal, igual para o mesmo valor. */
    public String indice(String texto) {
        if (texto == null) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(chaveIndice);
            return HexFormat.of().formatHex(mac.doFinal(texto.getBytes(StandardCharsets.UTF_8)));

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível calcular o índice do dado sensível.", e);
        }
    }

    private static byte[] decodificarChave(String chaveBase64) {
        String mensagem = "Defina SIGEI_CHAVE_DADOS_SENSIVEIS com pelo menos 32 bytes aleatórios em Base64 " +
                "(gere com: openssl rand -base64 32).";

        if (chaveBase64 == null || chaveBase64.isBlank()) {
            throw new IllegalStateException(mensagem);
        }

        byte[] chave;
        try {
            chave = Base64.getDecoder().decode(chaveBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(mensagem, e);
        }

        if (chave.length < TAMANHO_MINIMO_CHAVE) {
            throw new IllegalStateException(mensagem);
        }

        return chave;
    }

    private static byte[] derivar(byte[] chaveMestra, String finalidade) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(chaveMestra, ALGORITMO_HMAC));
            return mac.doFinal(finalidade.getBytes(StandardCharsets.UTF_8));

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível derivar as chaves de criptografia.", e);
        }
    }
}
