package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.utils.MunicipioConversor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MunicipioService {
    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    MunicipioConversor municipioConversor;

    public MunicipioResponseDTO salvar(MunicipioRequestDTO municipioRequestDTO) throws DataIntegrityViolationException{
        Municipio municipio = municipioConversor.converterDTO(municipioRequestDTO);
        municipioRepository.save(municipio);

        return municipioConversor.converterMunicipio(municipio);
    }

    public void excluir(Long id){
        Optional<Municipio> optionalMunicipio = municipioRepository.findById(id);

        if (optionalMunicipio.isEmpty()){
            throw new IllegalArgumentException("O municipio não foi encontrado");
        } else {
            municipioRepository.deleteById(id);
        }
    }

}
