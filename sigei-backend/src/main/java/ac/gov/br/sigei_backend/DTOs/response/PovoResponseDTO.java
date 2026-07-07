package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.AldeiaResumoDTO;

import java.util.List;

public record PovoResponseDTO(

        Long id,

        String nome,

        List<AldeiaResumoDTO> aldeias

) {
}