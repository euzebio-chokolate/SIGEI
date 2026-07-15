package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;

public record InfraestruturaEscolaResponseDTO(

        Long id,
        EscolaResumoDTO escola,
        int anoReferencia,
        int anoConstrucao,
        int numSalas,
        Double latitude,
        Double longitude,
        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        Boolean temAgua,
        Boolean temInternet,
        Boolean temEnergia,
        Boolean temRegistroFotografico

) {
}