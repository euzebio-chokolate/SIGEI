package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.AldeiaResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.MunicipioResumoDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.TerraIndigenaResumoDTO;

public record EscolaDetalheResponseDTO(

        Long id,

        String codigo,

        String nome,

        String tipoUnidade,

        EscolaResumoDTO escolaSede,

        MunicipioResumoDTO municipio,

        TerraIndigenaResumoDTO terraIndigena,

        AldeiaResumoDTO aldeia,

        EquipeEscolaResponseDTO equipe,

        InfraestruturaEscolaResponseDTO infraestrutura,

        RegistroAnualResponseDTO registroAnual

) {
}