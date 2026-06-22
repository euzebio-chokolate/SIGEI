package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.AldeiaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AldeiaService {
    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    AldeiaMapper aldeiaMapper;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    public AldeiaResponseDTO salvar(AldeiaRequestDTO aldeiaRequestDTO) throws DataIntegrityViolationException{
        Optional<TerraIndigena> optionalTerraIndigena = terraIndigenaRepository.findById(aldeiaRequestDTO.terraIndigenaId());
        if (optionalTerraIndigena.isEmpty()){
            throw new NoSuchElementException("Terra indígena não encontrada");
        }

        Aldeia aldeia = new Aldeia();
        aldeia.setNome(aldeiaRequestDTO.nome());
        aldeia.setTerraIndigena(optionalTerraIndigena.get());

        Aldeia aldeiaSalva = aldeiaRepository.save(aldeia);
        return aldeiaMapper.converter(aldeiaSalva);
    }

    public List<AldeiaResponseDTO> buscarTudo(){
        List<Aldeia> aldeias = aldeiaRepository.findAll();
        List<AldeiaResponseDTO> aldeiaResponseDTOS = new ArrayList<>();
        for (Aldeia aldeia : aldeias){
            AldeiaResponseDTO aldeiaResponseDTO = aldeiaMapper.converter(aldeia);
            aldeiaResponseDTOS.add(aldeiaResponseDTO);
        }
        return aldeiaResponseDTOS;
    }

    public AldeiaResponseDTO editar(AldeiaRequestDTO aldeiaDTO){
        Optional<Aldeia> optionalAldeia = aldeiaRepository.findById(aldeiaDTO.id());
        if (optionalAldeia.isEmpty()){
            throw new NoSuchElementException("Id de aldeia inválido ou não encontrado ");
        }

        Optional<TerraIndigena> optionalTerraIndigena = terraIndigenaRepository.findById(aldeiaDTO.terraIndigenaId());
        if (optionalTerraIndigena.isEmpty()){
            throw new NoSuchElementException("Id de terra indígena inválido ou não encontrado");
        }

        Aldeia aldeia = optionalAldeia.get();
        TerraIndigena terraIndigena = optionalTerraIndigena.get();

        aldeia.setNome(aldeiaDTO.nome());
        aldeia.setTerraIndigena(terraIndigena);
        aldeiaRepository.save(aldeia);

        Aldeia aldeiaSalva = aldeiaRepository.save(aldeia);
        return aldeiaMapper.converter(aldeiaSalva);
    }

    public void excluir(Long id){
        Optional<Aldeia> optionalAldeia = aldeiaRepository.findById(id);
        aldeiaRepository.deleteById(id);
    }
}
