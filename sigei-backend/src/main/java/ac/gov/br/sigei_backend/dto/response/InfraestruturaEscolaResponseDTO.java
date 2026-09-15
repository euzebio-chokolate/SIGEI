package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.RegistroAnualResumoDTO;

public record InfraestruturaEscolaResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        AnoReferenciaResumoDTO anoReferencia,
        Integer anoConstrucao,
        Integer numSalas,
        Double latitude,
        Double longitude,
        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        String tipoAgua,
        Boolean temInternet,
        String tipoEnergia,
        Boolean temRegistroFotografico,
        String fotoUrl

) {
}