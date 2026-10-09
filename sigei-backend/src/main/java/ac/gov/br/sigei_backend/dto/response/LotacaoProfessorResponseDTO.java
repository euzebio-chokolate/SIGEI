package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.MunicipioResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.ProfessorResumoDTO;

import java.time.LocalDate;

/**
 * professor nulo indica vaga em aberto; escola nula, lotação no núcleo do município.
 * numeroContrato só é preenchido para o perfil ADMIN.
 */
public record LotacaoProfessorResponseDTO(

        Long id,

        ProfessorResumoDTO professor,

        AnoReferenciaResumoDTO anoReferencia,

        EscolaResumoDTO escola,

        MunicipioResumoDTO municipio,

        String cargo,

        String segmento,

        String numeroContrato,

        LocalDate inicioContrato,

        String amparoLegal,

        String tipoContrato

) {

    public LotacaoProfessorResponseDTO semDadosSensiveis() {
        return new LotacaoProfessorResponseDTO(
                id, professor, anoReferencia, escola, municipio, cargo, segmento,
                null, inicioContrato, amparoLegal, tipoContrato
        );
    }
}
