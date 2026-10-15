package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.service.armazenamento.CloudinaryService;
import ac.gov.br.sigei_backend.dto.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import ac.gov.br.sigei_backend.mappers.InfraestruturaEscolaMapper;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.InfraestruturaEscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.utils.OpcoesInfraestrutura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class InfraestruturaEscolaService {

    private static final String INFRAESTRUTURA_DUPLICADA =
            "Este registro anual já tem uma infraestrutura cadastrada; edite a existente.";

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    InfraestruturaEscolaRepository infraestruturaRepository;

    @Autowired
    InfraestruturaEscolaMapper infraestruturaMapper;

    public InfraestruturaEscolaResponseDTO salvar(InfraestruturaEscolaRequestDTO infraDTO) {

        RegistroAnual registroAnual = registroAnualRepository.findById(infraDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        if (infraestruturaRepository.existsByRegistroAnual_Id(registroAnual.getId())) {
            throw new RegraNegocioException(INFRAESTRUTURA_DUPLICADA);
        }

        InfraestruturaEscola infraestrutura = infraestruturaMapper.converter(infraDTO);
        OpcoesInfraestrutura.validar(infraestrutura);
        infraestrutura.setRegistroAnual(registroAnual);
        InfraestruturaEscola infraestruturaSalva = infraestruturaRepository.save(infraestrutura);

        return infraestruturaMapper.converter(infraestruturaSalva);
    }

    public List<InfraestruturaEscolaResponseDTO> buscarTudo() {

        return infraestruturaRepository.findAll()
                .stream()
                .map(infraestruturaMapper::converter)
                .toList();
    }

    public List<InfraestruturaEscolaResponseDTO> buscarPorAno(Integer ano) {

        List<InfraestruturaEscola> infraestruturas =
                infraestruturaRepository
                        .findByRegistroAnual_AnoReferencia_Ano(ano);

        return infraestruturas.stream()
                .map(infraestruturaMapper::converter)
                .toList();
    }

    public List<InfraestruturaEscolaResponseDTO> buscarMaisRecentes() {
        return infraestruturaRepository.buscarMaisRecentesPorEscola();
    }

    public InfraestruturaEscolaResponseDTO editar(InfraestruturaEscolaRequestDTO infraDTO) {

        InfraestruturaEscola infraestrutura = infraestruturaRepository.findById(infraDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Infraestrutura não encontrada"));

        RegistroAnual registroAnual = registroAnualRepository.findById(infraDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        if (infraestruturaRepository.existsByRegistroAnual_IdAndIdNot(registroAnual.getId(), infraestrutura.getId())) {
            throw new RegraNegocioException(INFRAESTRUTURA_DUPLICADA);
        }

        infraestruturaMapper.atualizar(infraDTO, infraestrutura);
        OpcoesInfraestrutura.validar(infraestrutura);

        infraestrutura.setRegistroAnual(registroAnual);

        InfraestruturaEscola infraestruturaSalva = infraestruturaRepository.save(infraestrutura);

        return infraestruturaMapper.converter(infraestruturaSalva);
    }

    public InfraestruturaEscolaResponseDTO adicionarFoto(
            Long infraestruturaId,
            MultipartFile foto
    ) throws IOException {

        InfraestruturaEscola infraestrutura =
                infraestruturaRepository.findById(infraestruturaId)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Infraestrutura não encontrada"
                                ));

        Map resultado = cloudinaryService.uploadImagem(foto);

        infraestrutura.setFotoUrl(
                resultado.get("secure_url").toString()
        );

        infraestrutura.setFotoPublicId(
                resultado.get("public_id").toString()
        );

        InfraestruturaEscola infraestruturaSalva =
                infraestruturaRepository.save(infraestrutura);

        return infraestruturaMapper.converter(infraestruturaSalva);
    }

    public void excluir(Long id) {

        infraestruturaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Infraestrutura não encontrada"));

        infraestruturaRepository.deleteById(id);
    }
}
