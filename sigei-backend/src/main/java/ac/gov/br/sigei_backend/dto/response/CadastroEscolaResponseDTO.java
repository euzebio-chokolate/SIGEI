package ac.gov.br.sigei_backend.dto.response;

public record CadastroEscolaResponseDTO(
        EscolaResponseDTO escola,
        RegistroAnualResponseDTO registro
) {
}
