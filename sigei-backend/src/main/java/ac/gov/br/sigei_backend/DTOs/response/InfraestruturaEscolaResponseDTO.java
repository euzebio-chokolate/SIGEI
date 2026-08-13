package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.RegistroAnualResumoDTO;

public record InfraestruturaEscolaResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        AnoReferenciaResumoDTO anoReferencia,
        int anoConstrucao,
        int numSalas,
        Double latitude,
        Double longitude,
        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        String tipoAgua,
        Boolean temInternet,
        String tipoEnergia,
        Boolean temRegistroFotografico

) {
}