package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.EscolaResumoDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.RegistroAnualResumoDTO;

public record InfraestruturaEscolaResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        AnoReferenciaResumoDTO anoReferencia,
        Integer anoConstrucao,
        Integer numSalas,
        Double latitude,
        Double longitude,
        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        String tipoAgua,
        Boolean temInternet,
        String tipoEnergia,
        Boolean temRegistroFotografico,
        String fotoUrl

) {
    /** Projeção do mapa: seleciona apenas os campos da resposta, sem carregar entidades JPA. */
    public InfraestruturaEscolaResponseDTO(
            Long id, Long escolaId, String escolaCodigo, String escolaNome,
            Long anoReferenciaId, Integer anoReferenciaAno,
            Integer anoConstrucao, Integer numSalas, Double latitude, Double longitude,
            String situacao, String tipoAcesso, Boolean temPredio, String tipoAgua,
            Boolean temInternet, String tipoEnergia, Boolean temRegistroFotografico, String fotoUrl
    ) {
        this(id,
                escolaId == null ? null : new EscolaResumoDTO(escolaId, escolaCodigo, escolaNome),
                anoReferenciaId == null ? null : new AnoReferenciaResumoDTO(anoReferenciaId, anoReferenciaAno),
                anoConstrucao, numSalas, latitude, longitude, situacao, tipoAcesso,
                temPredio, tipoAgua, temInternet, tipoEnergia, temRegistroFotografico, fotoUrl);
    }
}
