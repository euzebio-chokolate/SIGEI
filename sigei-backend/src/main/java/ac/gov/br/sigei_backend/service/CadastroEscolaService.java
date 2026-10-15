package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.CadastroEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.CadastroEscolaMapper;
import ac.gov.br.sigei_backend.mappers.EscolaMapper;
import ac.gov.br.sigei_backend.mappers.RegistroAnualMapper;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.service.armazenamento.CloudinaryService;
import ac.gov.br.sigei_backend.utils.OpcoesInfraestrutura;
import ac.gov.br.sigei_backend.utils.OpcoesRegistroAnual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Grava o cadastro completo de uma escola (escola, registro anual, equipe,
 * infraestrutura, matrículas e foto) em uma única transação: ou tudo é
 * persistido, ou nada é.
 */
@Service
public class CadastroEscolaService {

    private static final Logger log = LoggerFactory.getLogger(CadastroEscolaService.class);

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    VinculoSedeService vinculoSedeService;

    @Autowired
    EscolaMapper escolaMapper;

    @Autowired
    RegistroAnualMapper registroAnualMapper;

    @Autowired
    CadastroEscolaMapper cadastroEscolaMapper;

    // IOException é verificada: sem rollbackFor, uma falha no upload gravaria o cadastro sem a foto
    @Transactional(rollbackFor = IOException.class)
    public CadastroEscolaResponseDTO salvar(CadastroEscolaRequestDTO dto, MultipartFile foto) throws IOException {
        if (dto.escola().id() != null) {
            throw new IllegalArgumentException("Uma nova escola não deve informar ID; use a edição do cadastro.");
        }

        Escola escola = escolaMapper.converter(dto.escola());
        vincularRelacionamentos(escola, dto.escola());
        Escola escolaSalva = escolaRepository.save(escola);

        return gravarRegistroAnual(escolaSalva, dto, foto);
    }

    @Transactional(rollbackFor = IOException.class)
    public CadastroEscolaResponseDTO editar(CadastroEscolaRequestDTO dto, MultipartFile foto) throws IOException {
        if (dto.escola().id() == null) {
            throw new IllegalArgumentException("O ID da escola é obrigatório na edição.");
        }

        Escola escola = escolaRepository.findById(dto.escola().id())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        escolaMapper.atualizar(dto.escola(), escola);
        vincularRelacionamentos(escola, dto.escola());
        Escola escolaSalva = escolaRepository.save(escola);

        return gravarRegistroAnual(escolaSalva, dto, foto);
    }

    private void vincularRelacionamentos(Escola escola, EscolaRequestDTO dto) {
        Aldeia aldeia = aldeiaRepository.findById(dto.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));
        vinculoSedeService.verificarAldeiaLivre(aldeia.getId(), escola.getId());

        escola.setAldeia(aldeia);
        vinculoSedeService.vincular(escola, dto.tipoUnidade(), dto.escolaSedeId());
    }

    /** Cria o registro do ano informado ou atualiza o existente, sem tocar nos demais anos. */
    private CadastroEscolaResponseDTO gravarRegistroAnual(
            Escola escola,
            CadastroEscolaRequestDTO dto,
            MultipartFile foto
    ) throws IOException {
        AnoReferencia anoReferencia = anoReferenciaRepository.findById(dto.anoReferenciaId())
                .orElseThrow(() -> new NoSuchElementException("Ano de referência não encontrado"));

        RegistroAnual registro = registroAnualRepository
                .findByEscolaAndAnoReferencia(escola, anoReferencia)
                .orElseGet(() -> novoRegistro(escola, anoReferencia));

        registro.setRede(OpcoesRegistroAnual.rede(dto.rede()));
        registro.setLocalFuncionamento(OpcoesRegistroAnual.localFuncionamento(dto.localFuncionamento()));
        if (dto.totalAlunos() != null) {
            registro.setTotalAlunos(dto.totalAlunos());
        }

        atualizarEquipe(registro, anoReferencia, dto.equipe());
        InfraestruturaEscola infraestrutura = atualizarInfraestrutura(registro, dto.infraestrutura(), dto.removerFoto());
        atualizarMatriculasNivel(registro, dto.matriculasNivel());
        atualizarMatriculasSerie(registro, dto.matriculasSerie());

        // Grava e valida as restrições do banco antes de enviar a foto,
        // evitando upload de imagem para um cadastro que seria rejeitado.
        RegistroAnual registroSalvo = registroAnualRepository.saveAndFlush(registro);

        if (foto != null && !foto.isEmpty()) {
            enviarFoto(infraestrutura, foto);
        }

        return new CadastroEscolaResponseDTO(
                escolaMapper.converter(escola),
                registroAnualMapper.converter(registroSalvo)
        );
    }

    private RegistroAnual novoRegistro(Escola escola, AnoReferencia anoReferencia) {
        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(anoReferencia);
        escola.getRegistros().add(registro);
        return registro;
    }

    private void atualizarEquipe(
            RegistroAnual registro,
            AnoReferencia anoReferencia,
            CadastroEscolaRequestDTO.EquipeDTO dto
    ) {
        EquipeEscola equipe = registro.getEquipe();
        if (equipe == null) {
            equipe = new EquipeEscola();
            equipe.setRegistroAnual(registro);
            registro.setEquipe(equipe);
        }

        cadastroEscolaMapper.atualizar(dto, equipe);
        equipe.setAnoReferencia(anoReferencia);
    }

    private InfraestruturaEscola atualizarInfraestrutura(
            RegistroAnual registro,
            CadastroEscolaRequestDTO.InfraestruturaDTO dto,
            boolean removerFoto
    ) {
        InfraestruturaEscola infraestrutura = registro.getInfraestrutura();
        if (infraestrutura == null) {
            infraestrutura = new InfraestruturaEscola();
            infraestrutura.setRegistroAnual(registro);
            registro.setInfraestrutura(infraestrutura);
        }

        cadastroEscolaMapper.atualizar(dto, infraestrutura);
        OpcoesInfraestrutura.validar(infraestrutura);

        if (removerFoto) {
            infraestrutura.setFotoUrl(null);
            infraestrutura.setFotoPublicId(null);
        }

        return infraestrutura;
    }

    /** Atualiza as matrículas pelo nível; níveis ausentes na requisição são preservados. */
    private void atualizarMatriculasNivel(
            RegistroAnual registro,
            List<CadastroEscolaRequestDTO.MatriculaNivelDTO> dtos
    ) {
        if (dtos == null) return;

        Map<String, MatriculaNivel> existentes = new HashMap<>();
        for (MatriculaNivel matricula : registro.getMatriculasNivel()) {
            existentes.putIfAbsent(matricula.getNivel(), matricula);
        }

        Map<String, Boolean> recebidos = new HashMap<>();
        for (CadastroEscolaRequestDTO.MatriculaNivelDTO dto : dtos) {
            if (recebidos.put(dto.nivel(), true) != null) {
                throw new IllegalArgumentException("Nível de matrícula repetido: " + dto.nivel());
            }

            MatriculaNivel matricula = existentes.get(dto.nivel());
            if (matricula == null) {
                matricula = new MatriculaNivel();
                matricula.setRegistro(registro);
                registro.getMatriculasNivel().add(matricula);
            }

            cadastroEscolaMapper.atualizar(dto, matricula);
        }
    }

    /** Atualiza as matrículas pela série/etapa; itens ausentes na requisição são preservados. */
    private void atualizarMatriculasSerie(
            RegistroAnual registro,
            List<CadastroEscolaRequestDTO.MatriculaSerieDTO> dtos
    ) {
        if (dtos == null) return;

        Map<String, MatriculaSerie> existentes = new HashMap<>();
        for (MatriculaSerie matricula : registro.getMatriculasSerie()) {
            existentes.putIfAbsent(chaveSerie(matricula.getDenominacao(), matricula.getEtapaEnsino()), matricula);
        }

        Map<String, Boolean> recebidos = new HashMap<>();
        for (CadastroEscolaRequestDTO.MatriculaSerieDTO dto : dtos) {
            String chave = chaveSerie(dto.denominacao(), dto.etapaEnsino());
            if (recebidos.put(chave, true) != null) {
                throw new IllegalArgumentException("Série de matrícula repetida: " + dto.denominacao());
            }

            MatriculaSerie matricula = existentes.get(chave);
            if (matricula == null) {
                matricula = new MatriculaSerie();
                matricula.setRegistro(registro);
                registro.getMatriculasSerie().add(matricula);
            }

            cadastroEscolaMapper.atualizar(dto, matricula);
        }
    }

    private String chaveSerie(String denominacao, String etapaEnsino) {
        return denominacao + "|" + Objects.toString(etapaEnsino, "");
    }

    private void enviarFoto(InfraestruturaEscola infraestrutura, MultipartFile foto) throws IOException {
        Map resultado = cloudinaryService.uploadImagem(foto);
        String publicId = resultado.get("public_id").toString();

        removerFotoSeTransacaoFalhar(publicId);

        infraestrutura.setFotoUrl(resultado.get("secure_url").toString());
        infraestrutura.setFotoPublicId(publicId);
    }

    /** O Cloudinary não participa da transação: compensa o upload se o banco desfizer o cadastro. */
    private void removerFotoSeTransacaoFalhar(String publicId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) return;
                try {
                    cloudinaryService.excluirImagem(publicId);
                } catch (Exception e) {
                    log.warn("Não foi possível remover a imagem {} após falha no cadastro da escola.", publicId, e);
                }
            }
        });
    }
}
