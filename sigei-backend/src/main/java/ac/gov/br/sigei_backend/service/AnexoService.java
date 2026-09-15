package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.AnexoRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnexoResponseDTO;
import ac.gov.br.sigei_backend.mappers.AnexoMapper;
import ac.gov.br.sigei_backend.models.Anexo;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.repository.AnexoRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnexoService {

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AnexoRepository anexoRepository;

    @Autowired
    AnexoMapper anexoMapper;

    public AnexoResponseDTO salvar(AnexoRequestDTO dto) throws DataIntegrityViolationException {

        Escola escola = escolaRepository.findById(dto.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        Escola escolaAnexo = escolaRepository.findById(dto.escolaAnexoId())
                .orElseThrow(() -> new NoSuchElementException("Escola anexo não encontrada"));

        Anexo anexo = anexoMapper.converter(dto);

        anexo.setEscola(escola);
        anexo.setEscolaAnexo(escolaAnexo);

        Anexo anexoSalvo = anexoRepository.save(anexo);
        return anexoMapper.converter(anexoSalvo);
    }

    public List<AnexoResponseDTO> buscarTudo() {
        return anexoRepository.findAll()
                .stream()
                .map(anexoMapper::converter)
                .toList();
    }

    public AnexoResponseDTO editar(AnexoRequestDTO dto) {

        Anexo anexo = anexoRepository.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("Anexo não encontrado"));

        Escola escola = escolaRepository.findById(dto.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        Escola escolaAnexo = escolaRepository.findById(dto.escolaAnexoId())
                .orElseThrow(() -> new NoSuchElementException("Escola anexo não encontrada"));

        anexoMapper.atualizar(dto, anexo);

        anexo.setEscola(escola);
        anexo.setEscolaAnexo(escolaAnexo);

        Anexo anexoSalvo = anexoRepository.save(anexo);

        return anexoMapper.converter(anexoSalvo);
    }

    public void excluir(Long id) {

        anexoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Anexo não encontrado"));

        anexoRepository.deleteById(id);
    }
}