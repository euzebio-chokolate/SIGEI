package ac.gov.br.sigei_backend.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Grava e remove os cookies da sessão. Os tokens ficam em cookies HttpOnly,
 * inacessíveis a scripts da página, e SameSite=Strict, enviados apenas em
 * requisições originadas no próprio site. O frontend deve acessar a API pela
 * mesma origem (proxy /api).
 */
@Component
public class CookiesSessao {

    public static final String COOKIE_ACESSO = "sigei_acesso";
    public static final String COOKIE_RENOVACAO = "sigei_renovacao";

    @Value("${jwt.expiration}")
    private Long expiracaoAcesso;

    @Value("${jwt.refresh-expiration}")
    private Long expiracaoRenovacao;

    @Value("${sigei.auth.cookie-secure:true}")
    private boolean seguro;

    public void gravar(HttpServletResponse response, String tokenAcesso, String refreshToken) {
        adicionar(response, COOKIE_ACESSO, tokenAcesso, Duration.ofMillis(expiracaoAcesso));
        adicionar(response, COOKIE_RENOVACAO, refreshToken, Duration.ofMillis(expiracaoRenovacao));
    }

    public void limpar(HttpServletResponse response) {
        adicionar(response, COOKIE_ACESSO, "", Duration.ZERO);
        adicionar(response, COOKIE_RENOVACAO, "", Duration.ZERO);
    }

    public static String ler(HttpServletRequest request, String nome) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (nome.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void adicionar(HttpServletResponse response, String nome, String valor, Duration duracao) {
        ResponseCookie cookie = ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(seguro)
                .sameSite("Strict")
                .path("/")
                .maxAge(duracao)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
