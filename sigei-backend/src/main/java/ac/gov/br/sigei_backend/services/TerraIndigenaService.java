package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.TerraIndigenaConversor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TerraIndigenaService {
    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    TerraIndigenaConversor terraIndigenaConversor;

    @Autowired
    MunicipioRepository municipioRepository;

    public TerraIndigenaResponseDTO salvar(TerraIndigenaRequestDTO terraIndigenaRequestDTO) throws DataIntegrityViolationException{
        Optional<Municipio> optionalMunicipio = municipioRepository.findById(terraIndigenaRequestDTO.municipioId());
        if (optionalMunicipio.isEmpty()){
            throw new RuntimeException("Município não encontrado");
        }

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome(terraIndigenaRequestDTO.nome());
        terraIndigena.setMunicipio(optionalMunicipio.get());

        TerraIndigena terraIndigenaSalva = terraIndigenaRepository.save(terraIndigena);
        return terraIndigenaConversor.converterTerraIndigena(terraIndigenaSalva);
    }

}
