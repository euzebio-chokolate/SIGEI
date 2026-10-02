package ac.gov.br.sigei_backend.service.auth;

import ac.gov.br.sigei_backend.dto.response.LoginResponseDTO;

/**
 * Resultado de login ou renovação. Os tokens são gravados em cookies HttpOnly
 * pelo controller e nunca seguem no corpo da resposta.
 */
public record SessaoAutenticada(
        LoginResponseDTO usuario,
        String tokenAcesso,
        String refreshToken
) {
}
