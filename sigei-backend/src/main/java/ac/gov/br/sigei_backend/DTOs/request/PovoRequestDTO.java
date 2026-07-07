package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PovoRequestDTO (
        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        String nome,
        List<Long> aldeiaIds
){
}
