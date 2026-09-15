package ac.gov.br.sigei_backend.dto.response;


import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;

public record LoginResponseDTO(

        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean alterarSenha,
        String token

) {
}