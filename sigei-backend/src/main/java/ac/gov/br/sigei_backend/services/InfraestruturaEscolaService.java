package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.InfraestruturaEscolaMapper;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.InfraestruturaEscolaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InfraestruturaEscolaService {

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    InfraestruturaEscolaRepository infraestruturaRepository;

    @Autowired
    InfraestruturaEscolaMapper infraestruturaMapper;

    public InfraestruturaEscolaResponseDTO salvar(InfraestruturaEscolaRequestDTO infraDTO){
        Escola escola = escolaRepository.findById(infraDTO.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        InfraestruturaEscola infraestrutura = infraestruturaMapper.converter(infraDTO);
        infraestrutura.setEscola(escola);

        InfraestruturaEscola infraestruturaSalva = infraestruturaRepository.save(infraestrutura);
        return infraestruturaMapper.converter(infraestruturaSalva);
    }

    public List<InfraestruturaEscolaResponseDTO> buscarTudo() {
        //testando esse metodo de buscar tudo
        return infraestruturaRepository.findAll().stream().map(infraestruturaMapper::converter).toList();
    }

    public InfraestruturaEscolaResponseDTO editar(InfraestruturaEscolaRequestDTO infraDTO) {
        InfraestruturaEscola infraestrutura = infraestruturaRepository.findById(infraDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Infraestrutura não encontrada"));

        Escola escola = escolaRepository.findById(infraDTO.escolaId())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        infraestruturaMapper.atualizar(infraDTO, infraestrutura);
        infraestrutura.setEscola(escola);

        InfraestruturaEscola infraestruturaSalva = infraestruturaRepository.save(infraestrutura);

        return infraestruturaMapper.converter(infraestruturaSalva);
    }

    public void excluir(Long id) {
        infraestruturaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Infraestrutura não encontrada"));

        infraestruturaRepository.deleteById(id);
    }
}
