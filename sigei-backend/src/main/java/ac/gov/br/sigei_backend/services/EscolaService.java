package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaDetalheResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.*;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EscolaService {
    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    EquipeEscolaRepository equipeRepository;

    @Autowired
    InfraestruturaEscolaRepository infraestruturaRepository;

    @Autowired
    RegistroAnualRepository registroRepository;

    @Autowired
    EscolaMapper escolaMapper;

    @Autowired
    MunicipioMapper municipioMapper;

    @Autowired
    TerraIndigenaMapper terraIndigenaMapper;

    @Autowired
    AldeiaMapper aldeiaMapper;

    @Autowired
    EquipeEscolaMapper equipeEscolaMapper;

    @Autowired
    InfraestruturaEscolaMapper infraestruturaEscolaMapper;

    @Autowired
    RegistroAnualMapper registroAnualMapper;

    public EscolaResponseDTO salvar(EscolaRequestDTO escolaRequestDTO) throws DataIntegrityViolationException{;
        Aldeia aldeia = aldeiaRepository.findById(escolaRequestDTO.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        Escola escolaSede = escolaRequestDTO.escolaSedeId() == null
                ? null
                : escolaRepository.findById(escolaRequestDTO.escolaSedeId())
                .orElseThrow(() -> new NoSuchElementException("Escola sede não encontrada"));

        Escola escola = escolaMapper.converter(escolaRequestDTO);

        escola.setAldeia(aldeia);
        escola.setEscolaSede(escolaSede);

        Escola escolaSalva = escolaRepository.save(escola);
        return escolaMapper.converter(escolaSalva);

    }

    public List<EscolaResponseDTO> buscarTudo(){
        List<Escola> escolas = escolaRepository.findAll();
        List<EscolaResponseDTO> escolaResponseDTOS = new ArrayList<>();
        for (Escola escola : escolas) {
            EscolaResponseDTO escolaResponseDTO = escolaMapper.converter(escola);
            escolaResponseDTOS.add(escolaResponseDTO);
        }
        return escolaResponseDTOS;
    }

    public EscolaDetalheResponseDTO buscarPorId(Long id) {

        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        EquipeEscola equipe = equipeRepository
                .findTopByEscolaOrderByAnoReferenciaDesc(escola)
                .orElse(null);

        InfraestruturaEscola infraestrutura = infraestruturaRepository
                .findTopByEscolaOrderByAnoReferenciaDesc(escola)
                .orElse(null);

        RegistroAnual registro = registroRepository
                .findTopByEscolaOrderByAnoDesc(escola)
                .orElse(null);

        return new EscolaDetalheResponseDTO(

                escola.getId(),
                escola.getCodigo(),
                escola.getNome(),
                escola.getTipoUnidade(),

                escola.getEscolaSede() == null
                        ? null
                        : escolaMapper.converterResumo(escola.getEscolaSede()),

                municipioMapper.converterResumo(
                        escola.getAldeia().getTerraIndigena().getMunicipio()
                ),

                terraIndigenaMapper.converterResumo(
                        escola.getAldeia().getTerraIndigena()
                ),

                aldeiaMapper.converterResumo(escola.getAldeia()),

                equipe == null ? null : equipeEscolaMapper.converter(equipe),

                infraestrutura == null ? null : infraestruturaEscolaMapper.converter(infraestrutura),

                registro == null ? null : registroAnualMapper.converter(registro)
        );
    }

    public EscolaResponseDTO editar(EscolaRequestDTO escolaDTO){
        Escola escola = escolaRepository.findById(escolaDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        Aldeia aldeia = aldeiaRepository.findById(escolaDTO.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        Escola escolaSede = escolaDTO.escolaSedeId() == null
                ? null
                : escolaRepository.findById(escolaDTO.escolaSedeId())
                .orElseThrow(() -> new NoSuchElementException("Escola sede não encontrada"));

        escolaMapper.atualizar(escolaDTO, escola);

        escola.setAldeia(aldeia);
        escola.setEscolaSede(escolaSede);

        Escola escolaSalva = escolaRepository.save(escola);

        return escolaMapper.converter(escolaSalva);
    }

    public void excluir(Long id){
        escolaRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Id de escola inválido ou não encontrado"));

        escolaRepository.deleteById(id);
    }
}
