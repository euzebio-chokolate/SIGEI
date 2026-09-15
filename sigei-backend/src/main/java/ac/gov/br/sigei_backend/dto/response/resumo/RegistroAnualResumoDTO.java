package ac.gov.br.sigei_backend.dto.response.resumo;

public record RegistroAnualResumoDTO(
        Long id,
        AnoReferenciaResumoDTO anoReferencia,
        String rede,
        String localFuncionamento
) {
}