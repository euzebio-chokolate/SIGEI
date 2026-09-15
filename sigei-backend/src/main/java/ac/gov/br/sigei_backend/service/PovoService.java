package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.dto.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.mappers.PovoMapper;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PovoService {

    @Autowired
    PovoRepository povoRepository;

    @Autowired
    PovoMapper povoMapper;

    public PovoResponseDTO salvar(PovoRequestDTO povoDTO) throws DataIntegrityViolationException {
        Povo povo = povoMapper.converter(povoDTO);
        Povo povoSalvo = povoRepository.save(povo);
        return povoMapper.converter(povoSalvo);
    }

    public List<PovoResponseDTO> buscarTudo() {
        List<Povo> povos = povoRepository.findAll();
        List<PovoResponseDTO> povoResponseDTOS = new ArrayList<>();

        for (Povo povo : povos) {
            PovoResponseDTO povoResponseDTO = povoMapper.converter(povo);
            povoResponseDTOS.add(povoResponseDTO);
        }

        return povoResponseDTOS;
    }

    public PovoResponseDTO buscarPorId(Long id) {
        Povo povo = povoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Povo não encontrado com ID fornecido"));

        return povoMapper.converter(povo);
    }

    public PovoResponseDTO editar(PovoRequestDTO povoDTO) {
        povoRepository.findById(povoDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Povo não encontrado"));

        Povo povo = povoMapper.converter(povoDTO);
        povo.setId(povoDTO.id());
        Povo povoSalvo = povoRepository.save(povo);
        return povoMapper.converter(povoSalvo);
    }

    public void excluir(Long id) {
        povoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de povo inválido ou não encontrado"));

        povoRepository.deleteById(id);
    }
}