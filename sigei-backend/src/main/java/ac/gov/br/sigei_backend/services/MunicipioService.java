package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MunicipioService {
    @Autowired
    MunicipioRepository municipioRepository;

    public MunicipioResponseDTO salvar(MunicipioRequestDTO municipioRequestDTO) throws DataIntegrityViolationException{
        //Optional<Municipio> optionalMunicipio =
    }


}
