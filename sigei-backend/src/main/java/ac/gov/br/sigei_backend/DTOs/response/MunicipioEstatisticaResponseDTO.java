package ac.gov.br.sigei_backend.DTOs.response;

public record MunicipioEstatisticaResponseDTO(

        Long id,
        String nome,
        Long quantidadeTerrasIndigenas,
        Long quantidadeAldeias,
        Long quantidadeEscolas

) {
}