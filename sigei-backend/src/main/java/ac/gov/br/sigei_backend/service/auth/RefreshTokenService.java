package ac.gov.br.sigei_backend.service.auth;

import ac.gov.br.sigei_backend.exceptions.TokenInvalidoException;
import ac.gov.br.sigei_backend.models.RefreshToken;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Emite, rotaciona e revoga refresh tokens. Cada token só pode ser usado uma
 * vez: ao renovar, o token apresentado é revogado e outro é emitido. Se um
 * token revogado há mais tempo que a tolerância for reapresentado (indício de
 * vazamento), todas as sessões do usuário são encerradas.
 */
@Service
public class RefreshTokenService {

    /**
     * Abas abertas compartilham o mesmo cookie e podem renovar ao mesmo tempo.
     * Dentro deste intervalo, reapresentar o token recém-rotacionado é tratado
     * como concorrência legítima, e não como vazamento.
     */
    static final Duration TOLERANCIA_CONCORRENCIA = Duration.ofSeconds(30);

    private static final SecureRandom GERADOR = new SecureRandom();

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private Long expiracao;

    @Transactional
    public String emitir(Usuario usuario) {
        Instant agora = Instant.now();
        refreshTokenRepository.excluirExpiradosDoUsuario(usuario, agora);

        byte[] bytes = new byte[32];
        GERADOR.nextBytes(bytes);
        String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(hash(valor));
        refreshToken.setCriadoEm(agora);
        refreshToken.setExpiraEm(agora.plusMillis(expiracao));
        refreshToken.setRevogado(false);
        refreshTokenRepository.save(refreshToken);

        return valor;
    }

    /**
     * Valida e consome o token, devolvendo o usuário dono dele. Não desfaz a
     * transação ao rejeitar o token, para manter as revogações registradas.
     */
    @Transactional(noRollbackFor = TokenInvalidoException.class)
    public Usuario consumir(String valor) {
        Instant agora = Instant.now();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(valor))
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida. Entre novamente."));

        Usuario usuario = refreshToken.getUsuario();

        if (Boolean.TRUE.equals(refreshToken.getRevogado())) {
            if (revogadoRecentemente(refreshToken, agora)) {
                throw new TokenInvalidoException("Sessão já renovada. Tente novamente.");
            }
            refreshTokenRepository.revogarTodosDoUsuario(usuario, agora);
            throw new TokenInvalidoException("Sessão encerrada. Entre novamente.");
        }

        if (!refreshToken.getExpiraEm().isAfter(agora)) {
            refreshToken.setRevogado(true);
            refreshToken.setRevogadoEm(agora);
            throw new TokenInvalidoException("Sessão expirada. Entre novamente.");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            refreshTokenRepository.revogarTodosDoUsuario(usuario, agora);
            throw new TokenInvalidoException("Usuário desativado.");
        }

        // Update condicional: entre duas renovações simultâneas do mesmo token, só uma vence
        if (refreshTokenRepository.revogarSeAtivo(refreshToken.getId(), agora) == 0) {
            throw new TokenInvalidoException("Sessão já renovada. Tente novamente.");
        }
        return usuario;
    }

    @Transactional
    public void revogar(String valor) {
        refreshTokenRepository.findByTokenHash(hash(valor))
                .filter(refreshToken -> !Boolean.TRUE.equals(refreshToken.getRevogado()))
                .ifPresent(refreshToken -> {
                    refreshToken.setRevogado(true);
                    refreshToken.setRevogadoEm(Instant.now());
                });
    }

    @Transactional
    public void revogarTodos(Usuario usuario) {
        refreshTokenRepository.revogarTodosDoUsuario(usuario, Instant.now());
    }

    private boolean revogadoRecentemente(RefreshToken refreshToken, Instant agora) {
        Instant revogadoEm = refreshToken.getRevogadoEm();
        return revogadoEm != null && revogadoEm.plus(TOLERANCIA_CONCORRENCIA).isAfter(agora);
    }

    static String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
