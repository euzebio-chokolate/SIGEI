package ac.gov.br.sigei_backend.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Decide se a resposta pode trazer os dados sensíveis dos professores (CPF,
 * matrícula e número do contrato): só o perfil ADMIN os recebe. A consulta de
 * professores e lotações continua liberada ao perfil USUARIO, sem esses campos.
 */
@Component
public class AcessoDadosSensiveis {

    public boolean podeVer() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        return autenticacao != null && autenticacao.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
