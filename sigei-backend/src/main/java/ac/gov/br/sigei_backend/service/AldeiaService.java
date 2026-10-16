package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.mappers.AldeiaMapper;
import ac.gov.br.sigei_backend.utils.Vinculos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AldeiaService {
    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    AldeiaMapper aldeiaMapper;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    PovoRepository povoRepository;

    @Autowired
    EscolaRepository escolaRepository;

    public AldeiaResponseDTO salvar(AldeiaRequestDTO dto) throws DataIntegrityViolationException {

        TerraIndigena terraIndigena = terraIndigenaRepository.findById(dto.terraIndigenaId())
                .orElseThrow(() -> new NoSuchElementException("Terra indígena não encontrada"));

        Povo povo = povoRepository.findById(dto.povoId())
                .orElseThrow(() -> new NoSuchElementException("Povo não encontrado"));

        Aldeia aldeia = aldeiaMapper.converter(dto);

        aldeia.setTerraIndigena(terraIndigena);
        aldeia.setPovo(povo);

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

    public AldeiaResponseDTO buscarPorId(Long id){
        Aldeia aldeia = aldeiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada com ID fornecido"));

        return aldeiaMapper.converter(aldeia);
    }

    public AldeiaResponseDTO editar(Long id, AldeiaRequestDTO dto) {

        aldeiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        TerraIndigena terraIndigena = terraIndigenaRepository.findById(dto.terraIndigenaId())
                .orElseThrow(() -> new NoSuchElementException("Terra indígena não encontrada"));

        Povo povo = povoRepository.findById(dto.povoId())
                .orElseThrow(() -> new NoSuchElementException("Povo não encontrado"));

        Aldeia aldeia = aldeiaMapper.converter(dto);

        aldeia.setId(id);
        aldeia.setTerraIndigena(terraIndigena);
        aldeia.setPovo(povo);

        Aldeia aldeiaSalva = aldeiaRepository.save(aldeia);

        return aldeiaMapper.converter(aldeiaSalva);
    }

    public void excluir(Long id) {
        aldeiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de aldeia inválido ou não encontrado"));

        Vinculos.para("Não é possível excluir a aldeia: ela tem")
                .com(escolaRepository.countByAldeia_Id(id), "escola vinculada", "escolas vinculadas")
                .verificar();

        aldeiaRepository.deleteById(id);
    }
}
