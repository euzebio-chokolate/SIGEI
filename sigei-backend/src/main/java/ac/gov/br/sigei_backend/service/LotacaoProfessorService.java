package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.config.security.AcessoDadosSensiveis;
import ac.gov.br.sigei_backend.dto.request.LotacaoProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LotacaoProfessorResponseDTO;
import ac.gov.br.sigei_backend.dto.response.PaginaDTO;
import ac.gov.br.sigei_backend.mappers.LotacaoProfessorMapper;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.LotacaoProfessor;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.utils.OpcoesProfessor;
import ac.gov.br.sigei_backend.utils.Paginacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class LotacaoProfessorService {

    // Agrupa por município e escola (núcleo primeiro); vagas sem professor ficam no fim
    private static final Comparator<LotacaoProfessor> ORDEM = Comparator
            .comparing((LotacaoProfessor l) -> l.getMunicipio().getNome(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(l -> l.getEscola() == null ? "" : l.getEscola().getNome(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(
                    l -> l.getProfessor() == null ? null : l.getProfessor().getNome(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );

    @Autowired
    LotacaoProfessorRepository lotacaoRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    LotacaoProfessorMapper lotacaoMapper;

    @Autowired
    AcessoDadosSensiveis acessoDadosSensiveis;

    @Transactional
    public LotacaoProfessorResponseDTO salvar(LotacaoProfessorRequestDTO lotacaoDTO) {
        LotacaoProfessor lotacao = new LotacaoProfessor();
        preencher(lotacaoDTO, lotacao);

        return converter(lotacaoRepository.save(lotacao));
    }

    /** Lotações de todos os anos, paginadas; a ordem é a de {@link #ORDEM}, do ano mais recente ao mais antigo. */
    public PaginaDTO<LotacaoProfessorResponseDTO> buscar(Integer ano, Long professorId, Long escolaId, Long municipioId,
                                                         int pagina, int tamanho) {
        Pageable pageable = Paginacao.pagina(pagina, tamanho);
        return PaginaDTO.de(lotacaoRepository.buscar(ano, professorId, escolaId, municipioId, pageable), this::converter);
    }

    public List<LotacaoProfessorResponseDTO> buscarPorAno(Integer ano) {
        if (ano == null) {
            throw new IllegalArgumentException("Informe o ano");
        }

        return converterTodas(lotacaoRepository.buscarPorAno(ano));
    }

    /** Lotações do professor em todos os anos, do mais recente ao mais antigo. */
    public List<LotacaoProfessorResponseDTO> buscarPorProfessor(Long professorId) {
        if (!professorRepository.existsById(professorId)) {
            throw new NoSuchElementException("Professor não encontrado");
        }

        return lotacaoRepository.findByProfessor_IdOrderByAnoReferencia_AnoDesc(professorId)
                .stream()
                .map(this::converter)
                .toList();
    }

    public LotacaoProfessorResponseDTO buscarPorId(Long id) {
        LotacaoProfessor lotacao = lotacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lotação não encontrada com ID fornecido"));

        return converter(lotacao);
    }

    @Transactional
    public LotacaoProfessorResponseDTO editar(Long id, LotacaoProfessorRequestDTO lotacaoDTO) {
        LotacaoProfessor lotacao = lotacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lotação não encontrada"));

        preencher(lotacaoDTO, lotacao);

        return converter(lotacaoRepository.save(lotacao));
    }

    @Transactional
    public void excluir(Long id) {
        LotacaoProfessor lotacao = lotacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de lotação inválido ou não encontrado"));

        lotacaoRepository.delete(lotacao);
    }

    /** Município da escola (pela aldeia e terra indígena), ou nulo se não houver. */
    public static Municipio municipioDaEscola(Escola escola) {
        if (escola == null
                || escola.getAldeia() == null
                || escola.getAldeia().getTerraIndigena() == null) {
            return null;
        }

        return escola.getAldeia().getTerraIndigena().getMunicipio();
    }

    private void preencher(LotacaoProfessorRequestDTO lotacaoDTO, LotacaoProfessor lotacao) {
        lotacaoMapper.atualizar(lotacaoDTO, lotacao);

        lotacao.setCargo(textoOuNulo(lotacaoDTO.cargo()));
        lotacao.setSegmento(textoOuNulo(lotacaoDTO.segmento()));
        lotacao.setNumeroContrato(textoOuNulo(lotacaoDTO.numeroContrato()));
        lotacao.setAmparoLegal(textoOuNulo(lotacaoDTO.amparoLegal()));
        lotacao.setTipoContrato(OpcoesProfessor.tipoContrato(lotacaoDTO.tipoContrato()));

        lotacao.setProfessor(
                lotacaoDTO.professorId() == null
                        ? null
                        : professorRepository.findById(lotacaoDTO.professorId())
                                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado"))
        );

        lotacao.setAnoReferencia(
                anoReferenciaRepository.findById(lotacaoDTO.anoReferenciaId())
                        .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"))
        );

        definirLocal(lotacaoDTO, lotacao);
    }

    /** Com escola, o município é o dela; sem escola (núcleo), o município é obrigatório. */
    private void definirLocal(LotacaoProfessorRequestDTO lotacaoDTO, LotacaoProfessor lotacao) {
        Escola escola = lotacaoDTO.escolaId() == null
                ? null
                : escolaRepository.findById(lotacaoDTO.escolaId())
                        .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        Municipio municipio = municipioDaEscola(escola);

        if (municipio == null) {
            if (lotacaoDTO.municipioId() == null) {
                throw new IllegalArgumentException(
                        "Informe a escola ou, para lotação no núcleo, o município"
                );
            }

            municipio = municipioRepository.findById(lotacaoDTO.municipioId())
                    .orElseThrow(() -> new NoSuchElementException("Município não encontrado"));
        }

        lotacao.setEscola(escola);
        lotacao.setMunicipio(municipio);
    }

    private List<LotacaoProfessorResponseDTO> converterTodas(List<LotacaoProfessor> lotacoes) {
        return lotacoes.stream()
                .sorted(ORDEM)
                .map(this::converter)
                .toList();
    }

    private LotacaoProfessorResponseDTO converter(LotacaoProfessor lotacao) {
        LotacaoProfessorResponseDTO resposta = lotacaoMapper.converter(lotacao);
        return acessoDadosSensiveis.podeVer() ? resposta : resposta.semDadosSensiveis();
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
