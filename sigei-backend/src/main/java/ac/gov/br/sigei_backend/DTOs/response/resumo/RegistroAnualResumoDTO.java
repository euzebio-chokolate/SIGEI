package ac.gov.br.sigei_backend.DTOs.response.resumo;

public record RegistroAnualResumoDTO(
        Long id,
        Integer ano,
        String rede,
        String localFuncionamento
) {
}