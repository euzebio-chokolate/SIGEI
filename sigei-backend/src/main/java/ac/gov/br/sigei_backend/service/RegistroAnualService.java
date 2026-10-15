package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import ac.gov.br.sigei_backend.mappers.RegistroAnualMapper;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.utils.OpcoesRegistroAnual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class RegistroAnualService {

    private static final String REGISTRO_DUPLICADO = "Já existe um registro anual para essa escola neste ano.";

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    RegistroAnualMapper registroAnualMapper;

    @Transactional
    public RegistroAnualResponseDTO salvar(RegistroAnualRequestDTO dto) {

        Escola escola = escolaRepository.findById(dto.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        AnoReferencia anoReferencia = anoReferenciaRepository.findById(dto.anoReferenciaId())
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        if (registroAnualRepository.existsByEscolaAndAnoReferencia(escola, anoReferencia)) {
            throw new RegraNegocioException(REGISTRO_DUPLICADO);
        }

        RegistroAnual registro = registroAnualMapper.converter(dto);
        aplicarOpcoes(registro);

        registro.setEscola(escola);
        registro.setAnoReferencia(anoReferencia);

        RegistroAnual registroSalvo = registroAnualRepository.save(registro);

        return registroAnualMapper.converter(registroSalvo);
    }

    public List<RegistroAnualResponseDTO> buscarTudo() {
        return registroAnualRepository.findAll()
                .stream()
                .map(registroAnualMapper::converter)
                .toList();
    }

    public RegistroAnualResponseDTO buscarPorEscolaEAno(Long escolaId, Integer ano) {

        Escola escola = escolaRepository.findById(escolaId)
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        RegistroAnual registro = registroAnualRepository
                .findByEscolaAndAnoReferencia_Ano(escola, ano)
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado para a escola no ano " + ano));

        return registroAnualMapper.converter(registro);
    }

    public List<RegistroAnualResponseDTO> buscarHistorico(Long escolaId, Integer anoInicial, Integer anoFinal) {
        if (escolaId == null || escolaId <= 0) {
            throw new IllegalArgumentException("ID da escola deve ser positivo");
        }
        if ((anoInicial != null && anoInicial <= 0) || (anoFinal != null && anoFinal <= 0)) {
            throw new IllegalArgumentException("Os anos do período devem ser positivos");
        }
        if (anoInicial != null && anoFinal != null && anoInicial > anoFinal) {
            throw new IllegalArgumentException("Ano inicial não pode ser maior que o ano final");
        }
        if (!escolaRepository.existsById(escolaId)) {
            throw new NoSuchElementException("Escola não encontrada");
        }
        return registroAnualRepository.buscarHistorico(escolaId, anoInicial, anoFinal)
                .stream()
                .map(registroAnualMapper::converter)
                .toList();
    }

    public List<RegistroAnualResponseDTO> buscarPorAno(Integer ano) {

        return registroAnualRepository
                .findByAnoReferencia_Ano(ano)
                .stream()
                .map(registroAnualMapper::converter)
                .toList();
    }

    @Transactional
    public RegistroAnualResponseDTO editar(RegistroAnualRequestDTO dto) {

        RegistroAnual registro = registroAnualRepository.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        Escola escola = escolaRepository.findById(dto.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        AnoReferencia anoReferencia = anoReferenciaRepository.findById(dto.anoReferenciaId())
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        if (registroAnualRepository.existsByEscolaAndAnoReferenciaAndIdNot(escola, anoReferencia, registro.getId())) {
            throw new RegraNegocioException(REGISTRO_DUPLICADO);
        }

        registroAnualMapper.atualizar(dto, registro);
        aplicarOpcoes(registro);

        registro.setEscola(escola);
        registro.setAnoReferencia(anoReferencia);

        RegistroAnual registroSalvo = registroAnualRepository.save(registro);

        return registroAnualMapper.converter(registroSalvo);
    }

    private void aplicarOpcoes(RegistroAnual registro) {
        registro.setRede(OpcoesRegistroAnual.rede(registro.getRede()));
        registro.setLocalFuncionamento(OpcoesRegistroAnual.localFuncionamento(registro.getLocalFuncionamento()));
    }

    @Transactional
    public void excluir(Long id) {

        registroAnualRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        registroAnualRepository.deleteById(id);
    }
}
