package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.RegistroAnualResumoDTO;

public record InfraestruturaEscolaResponseDTO(
        Long id,
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