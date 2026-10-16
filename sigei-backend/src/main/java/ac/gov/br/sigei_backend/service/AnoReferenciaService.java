package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnoReferenciaResponseDTO;
import ac.gov.br.sigei_backend.mappers.AnoReferenciaMapper;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.LotacaoProfessorRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.utils.Vinculos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnoReferenciaService {

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    AnoReferenciaMapper anoReferenciaMapper;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    LotacaoProfessorRepository lotacaoProfessorRepository;

    @Autowired
    AtualizacaoAnualService atualizacaoAnualService;

    public AnoReferenciaResponseDTO salvar(AnoReferenciaRequestDTO dto) {

        if (anoReferenciaRepository.existsByAno(dto.ano())) {
            throw new RegraNegocioException("O ano " + dto.ano() + " já está cadastrado.");
        }

        AnoReferencia anoReferencia = anoReferenciaMapper.converter(dto);

        AnoReferencia anoSalvo = anoReferenciaRepository.save(anoReferencia);

        return anoReferenciaMapper.converter(anoSalvo);
    }

    public AnoReferenciaResponseDTO iniciarProximo() {
        return anoReferenciaMapper.converter(atualizacaoAnualService.iniciarNovoAno());
    }

    public List<AnoReferenciaResponseDTO> buscarTudo() {
        return anoReferenciaRepository.findAll()
                .stream()
                .map(anoReferenciaMapper::converter)
                .toList();
    }

    public AnoReferenciaResponseDTO editar(Long id, AnoReferenciaRequestDTO dto) {

        AnoReferencia anoReferencia = anoReferenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        if (anoReferenciaRepository.existsByAnoAndIdNot(dto.ano(), anoReferencia.getId())) {
            throw new RegraNegocioException("O ano " + dto.ano() + " já está cadastrado.");
        }

        anoReferenciaMapper.atualizar(dto, anoReferencia);

        AnoReferencia anoSalvo = anoReferenciaRepository.save(anoReferencia);

        return anoReferenciaMapper.converter(anoSalvo);
    }

    public void excluir(Long id) {

        AnoReferencia anoReferencia = anoReferenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        Vinculos.para("Não é possível excluir o ano " + anoReferencia.getAno() + ": ele tem")
                .com(registroAnualRepository.countByAnoReferencia_Id(id),
                        "registro anual de escola", "registros anuais de escolas")
                .com(lotacaoProfessorRepository.countByAnoReferencia_Id(id),
                        "lotação de professor", "lotações de professores")
                .verificar();

        anoReferenciaRepository.deleteById(id);
    }
}