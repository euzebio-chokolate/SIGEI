package ac.gov.br.sigei_backend.DTOs;

public record EscolaDTO(
        Long id,
        String codigo,
        String nome,
        Long municipioId,
        Long terraIndigenaId,
        Long aldeiaId
) {

}
