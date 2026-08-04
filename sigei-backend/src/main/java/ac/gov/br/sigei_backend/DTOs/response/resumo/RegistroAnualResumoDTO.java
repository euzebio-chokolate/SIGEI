package ac.gov.br.sigei_backend.DTOs.response.resumo;

public record RegistroAnualResumoDTO(
        Long id,
        AnoReferenciaResumoDTO anoReferencia,
        String rede,
        String localFuncionamento
) {
}