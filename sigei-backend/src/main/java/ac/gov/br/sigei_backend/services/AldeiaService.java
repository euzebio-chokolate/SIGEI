package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.AldeiaConversor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AldeiaService {
    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    AldeiaConversor aldeiaConversor;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    public AldeiaResponseDTO salvar(AldeiaRequestDTO aldeiaRequestDTO) throws DataIntegrityViolationException{
        Optional<TerraIndigena> optionalTerraIndigena = terraIndigenaRepository.findById(aldeiaRequestDTO.terraIndigenaId());
        if (optionalTerraIndigena.isEmpty()){
            throw new RuntimeException("Terra indígena não encontrada");
        }

        Aldeia aldeia = new Aldeia();
        aldeia.setNome(aldeiaRequestDTO.nome());
        aldeia.setTerraIndigena(optionalTerraIndigena.get());

        Aldeia aldeiaSalva = aldeiaRepository.save(aldeia);
        return aldeiaConversor.converterAldeia(aldeiaSalva);
    }

}
