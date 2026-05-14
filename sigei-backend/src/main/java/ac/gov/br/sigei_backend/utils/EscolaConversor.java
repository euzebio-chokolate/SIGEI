package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.stereotype.Component;

@Component
public class EscolaConversor {
    public EscolaResponseDTO converterEscola(Escola escola) {

        MunicipioResponseDTO municipioDTO = new MunicipioResponseDTO(
                escola.getMunicipio().getId(),
                escola.getMunicipio().getNome()
        );

        TerraIndigenaResponseDTO terraIndigenaDTO = new TerraIndigenaResponseDTO(
                escola.getTerraIndigena().getId(),
                escola.getTerraIndigena().getNome(),
                municipioDTO
        );

        AldeiaResponseDTO aldeiaDTO = new AldeiaResponseDTO(
                escola.getAldeia().getId(),
                escola.getAldeia().getNome(),
                terraIndigenaDTO
        );

        EscolaResponseDTO escolaResponseDTO = new EscolaResponseDTO(
                escola.getId(),
                escola.getCodigo(),
                escola.getNome(),
                municipioDTO,
                aldeiaDTO
        );
        return escolaResponseDTO;
    }
}
