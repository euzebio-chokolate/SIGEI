package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.stereotype.Component;

@Component
public class EscolaConversor {
    public EscolaResponseDTO converterEscola(Escola escola){
        EscolaResponseDTO escolaResponseDTO = new EscolaResponseDTO(
                escola.getId(),
                escola.getCodigo(),
                escola.getNome(),
                escola.getMunicipio(),
                escola.getTerraIndigena(),
                escola.getAldeia()
        );
        return escolaResponseDTO;
    }
}
