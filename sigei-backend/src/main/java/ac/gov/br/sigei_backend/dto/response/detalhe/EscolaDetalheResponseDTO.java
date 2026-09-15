package ac.gov.br.sigei_backend.dto.response.detalhe;

import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.AldeiaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.MunicipioResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.TerraIndigenaResumoDTO;

public record EscolaDetalheResponseDTO(
        Long id,
        String codigo,
        String nome,
        String tipoUnidade,
        EscolaResumoDTO escolaSede,
        MunicipioResumoDTO municipio,
        TerraIndigenaResumoDTO terraIndigena,
        AldeiaResumoDTO aldeia,
        RegistroAnualResponseDTO registroAnual

) {}