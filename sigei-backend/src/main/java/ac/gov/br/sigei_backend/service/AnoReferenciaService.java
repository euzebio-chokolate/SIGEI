package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnoReferenciaResponseDTO;
import ac.gov.br.sigei_backend.mappers.AnoReferenciaMapper;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnoReferenciaService {

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    AnoReferenciaMapper anoReferenciaMapper;

    public AnoReferenciaResponseDTO salvar(AnoReferenciaRequestDTO dto)
            throws DataIntegrityViolationException {

        AnoReferencia anoReferencia = anoReferenciaMapper.converter(dto);

        AnoReferencia anoSalvo = anoReferenciaRepository.save(anoReferencia);

        return anoReferenciaMapper.converter(anoSalvo);
    }

    public List<AnoReferenciaResponseDTO> buscarTudo() {
        return anoReferenciaRepository.findAll()
                .stream()
                .map(anoReferenciaMapper::converter)
                .toList();
    }

    public AnoReferenciaResponseDTO editar(AnoReferenciaRequestDTO dto) {

        AnoReferencia anoReferencia = anoReferenciaRepository.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        anoReferenciaMapper.atualizar(dto, anoReferencia);

        AnoReferencia anoSalvo = anoReferenciaRepository.save(anoReferencia);

        return anoReferenciaMapper.converter(anoSalvo);
    }

    public void excluir(Long id) {

        anoReferenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        anoReferenciaRepository.deleteById(id);
    }
}