package ac.gov.br.sigei_backend.config.security;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import ac.gov.br.sigei_backend.utils.EmailUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extrairToken(request);

        if (token == null || !jwtService.tokenValido(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        String email =
                jwtService.extrairEmail(token);

        Usuario usuario = usuarioRepository
                .findByEmail(EmailUtils.normalizar(email))
                .orElse(null);

        if (usuario != null &&
                usuario.getAtivo() &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            SimpleGrantedAuthority autoridade =
                    new SimpleGrantedAuthority(
                            Boolean.TRUE.equals(usuario.getAlterarSenha())
                                    ? "ROLE_TROCAR_SENHA"
                                    : "ROLE_" + usuario.getPerfil().name()
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            List.of(autoridade)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * O navegador envia o token no cookie HttpOnly; clientes de API (Bruno,
     * scripts) podem usar o cabeçalho Bearer, que tem precedência.
     */
    private String extrairToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return CookiesSessao.ler(request, CookiesSessao.COOKIE_ACESSO);
    }
}
