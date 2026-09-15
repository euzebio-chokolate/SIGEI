package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.EquipeEscolaMapper;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.EquipeEscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class EquipeEscolaService {

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    EquipeEscolaRepository equipeEscolaRepository;

    @Autowired
    EquipeEscolaMapper equipeEscolaMapper;

    public EquipeEscolaResponseDTO salvar(EquipeEscolaRequestDTO equipeDTO)
            throws DataIntegrityViolationException {

        RegistroAnual registroAnual = registroAnualRepository
                .findById(equipeDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        EquipeEscola equipeEscola = equipeEscolaMapper.converter(equipeDTO);

        equipeEscola.setRegistroAnual(registroAnual);

        EquipeEscola equipeEscolaSalva = equipeEscolaRepository.save(equipeEscola);

        return equipeEscolaMapper.converter(equipeEscolaSalva);
    }

    public EquipeEscolaResponseDTO editar(EquipeEscolaRequestDTO equipeDTO) {

        EquipeEscola equipeEscola = equipeEscolaRepository.findById(equipeDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Equipe escolar não encontrada"));

        RegistroAnual registroAnual = registroAnualRepository
                .findById(equipeDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        equipeEscolaMapper.atualizar(equipeDTO, equipeEscola);

        equipeEscola.setRegistroAnual(registroAnual);

        EquipeEscola equipeEscolaSalva = equipeEscolaRepository.save(equipeEscola);

        return equipeEscolaMapper.converter(equipeEscolaSalva);
    }

    public void excluir(Long id) {

        equipeEscolaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipe escolar não encontrada"));

        equipeEscolaRepository.deleteById(id);
    }
}