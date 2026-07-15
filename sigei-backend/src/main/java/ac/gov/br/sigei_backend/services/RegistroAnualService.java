package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.mappers.RegistroAnualMapper;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RegistroAnualService {

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    RegistroAnualMapper registroAnualMapper;

    public RegistroAnualResponseDTO salvar(RegistroAnualRequestDTO dto) throws DataIntegrityViolationException {

        Escola escola = escolaRepository.findById(dto.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        RegistroAnual registro = registroAnualMapper.converter(dto);
        registro.setEscola(escola);
        RegistroAnual registroSalvo = registroAnualRepository.save(registro);

        return registroAnualMapper.converter(registroSalvo);
    }

    public List<RegistroAnualResponseDTO> buscarTudo() {
        return registroAnualRepository.findAll().stream().map(registroAnualMapper::converter).toList();
    }

    public RegistroAnualResponseDTO editar(RegistroAnualRequestDTO dto) {
        RegistroAnual registro = registroAnualRepository.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        Escola escola = escolaRepository.findById(dto.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        registroAnualMapper.atualizar(dto, registro);
        registro.setEscola(escola);
        RegistroAnual registroSalvo = registroAnualRepository.save(registro);

        return registroAnualMapper.converter(registroSalvo);
    }

    public void excluir(Long id) {
        registroAnualRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        registroAnualRepository.deleteById(id);
    }
}