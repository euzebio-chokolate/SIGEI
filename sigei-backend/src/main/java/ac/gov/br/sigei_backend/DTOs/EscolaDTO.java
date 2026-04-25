package ac.gov.br.sigei_backend.DTOs;

import ac.gov.br.sigei_backend.models.Escola;

public record EscolaDTO(
        Long id,
        String nome,
        Double latitude,
        Double longitude
) {


    public Escola ConverterParaEntidade(){
        Escola escola = new Escola();
        escola.setId(id);
        escola.setNome(nome);
        escola.setLatitude(latitude);
        escola.setLongitude(longitude);
        return escola;
    }

}
