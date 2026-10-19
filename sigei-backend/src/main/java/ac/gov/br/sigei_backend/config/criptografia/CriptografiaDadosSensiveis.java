package ac.gov.br.sigei_backend.config.criptografia;

import org.springframework.beans.factory.annotation.Autowired;
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
 * <p>As chaves de cifra e de índice são derivadas da chave de dados, que
 * {@link GerenciadorChaveDados} guarda no banco cifrada pela chave mestra
 * {@code SIGEI_CHAVE_DADOS_SENSIVEIS} e pela chave de recuperação (cifra em
 * envelope). Trocar a chave mestra não muda a chave de dados, então os valores
 * gravados não precisam ser recifrados.
 */
@Component
public class CriptografiaDadosSensiveis {

    private static final String PREFIXO = "v1:";
    private static final String ALGORITMO_CIFRA = "AES/GCM/NoPadding";
    private static final String ALGORITMO_HMAC = "HmacSHA256";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    private final SecretKey chaveCifra;
    private final SecretKey chaveIndice;
    private final SecureRandom aleatorio = new SecureRandom();

    /** Na aplicação: a chave de dados resolvida pelo envelope. */
    @Autowired
    public CriptografiaDadosSensiveis(GerenciadorChaveDados gerenciador) {
        this(gerenciador.chaveDados());
    }

    /**
     * Com a chave de dados em Base64. Fora do envelope, só para testes e para
     * fatias JPA sem o gerenciador ({@link DadoSensivelConverter}); na primeira
     * inicialização, a chave de dados é igual à chave mestra.
     */
    public CriptografiaDadosSensiveis(String chaveBase64) {
        this(EnvelopeChave.decodificarChaveMestra(chaveBase64, GerenciadorChaveDados.VARIAVEL_MESTRA));
    }

    CriptografiaDadosSensiveis(byte[] chaveDados) {
        this.chaveCifra = new SecretKeySpec(
                derivar(chaveDados, "sigei-dados-sensiveis-cifra"),
                "AES"
        );
        this.chaveIndice = new SecretKeySpec(
                derivar(chaveDados, "sigei-dados-sensiveis-indice"),
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

    static byte[] derivar(byte[] chave, String finalidade) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(chave, ALGORITMO_HMAC));
            return mac.doFinal(finalidade.getBytes(StandardCharsets.UTF_8));

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível derivar as chaves de criptografia.", e);
        }
    }
}
