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
        TerraIndigena terraIndigena = terraIndigenaRepository.findById(aldeiaRequestDTO.terraIndigenaId())
                .orElseThrow(() -> new NoSuchElementException("Terra índigena não encontrada"));

        Aldeia aldeia = aldeiaMapper.converter(aldeiaRequestDTO);
        aldeia.setTerraIndigena(terraIndigena);
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

    public AldeiaResponseDTO editar(AldeiaRequestDTO aldeiaDTO) {

        aldeiaRepository.findById(aldeiaDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Id de aldeia inválido ou não encontrado"));

        TerraIndigena terraIndigena = terraIndigenaRepository.findById(aldeiaDTO.terraIndigenaId())
                .orElseThrow(() -> new NoSuchElementException("Id de terra indígena inválido ou não encontrado"));

        Aldeia aldeia = aldeiaMapper.converter(aldeiaDTO);
        aldeia.setId(aldeiaDTO.id());
        aldeia.setTerraIndigena(terraIndigena);

        Aldeia aldeiaSalva = aldeiaRepository.save(aldeia);

        return aldeiaMapper.converter(aldeiaSalva);
    }

    public void excluir(Long id) {
        aldeiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de aldeia inválido ou não encontrado"));

        aldeiaRepository.deleteById(id);
    }
}
