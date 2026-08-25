package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.InfraestruturaEscolaMapper;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.InfraestruturaEscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class InfraestruturaEscolaService {

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

        InfraestruturaEscola infraestrutura = infraestruturaMapper.converter(infraDTO);
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

    public InfraestruturaEscolaResponseDTO editar(InfraestruturaEscolaRequestDTO infraDTO) {

        InfraestruturaEscola infraestrutura = infraestruturaRepository.findById(infraDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Infraestrutura não encontrada"));

        RegistroAnual registroAnual = registroAnualRepository.findById(infraDTO.registroAnualId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        infraestruturaMapper.atualizar(infraDTO, infraestrutura);

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