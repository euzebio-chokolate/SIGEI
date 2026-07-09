package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.EquipeEscolaMapper;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.repository.EquipeEscolaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class EquipeEscolaService {
    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    EquipeEscolaRepository equipeEscolaRepository;

    @Autowired
    EquipeEscolaMapper equipeEscolaMapper;

    public EquipeEscolaResponseDTO salvar(EquipeEscolaRequestDTO equipeDTO) throws DataIntegrityViolationException {

        Escola escola = escolaRepository.findById(equipeDTO.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        EquipeEscola equipeEscola = equipeEscolaMapper.converter(equipeDTO);
        equipeEscola.setEscola(escola);
        EquipeEscola equipeEscolaSalva = equipeEscolaRepository.save(equipeEscola);

        return equipeEscolaMapper.converter(equipeEscolaSalva);
    }

    public EquipeEscolaResponseDTO editar(EquipeEscolaRequestDTO equipeDTO) {

        EquipeEscola equipeEscola = equipeEscolaRepository.findById(equipeDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Equipe escolar não encontrada"));

        Escola escola = escolaRepository.findById(equipeDTO.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        equipeEscolaMapper.atualizar(equipeDTO, equipeEscola);

        equipeEscola.setEscola(escola);

        EquipeEscola equipeEscolaSalva = equipeEscolaRepository.save(equipeEscola);

        return equipeEscolaMapper.converter(equipeEscolaSalva);
    }

}
