package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.detalhe.EscolaDetalheResponseDTO;
import ac.gov.br.sigei_backend.mappers.*;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import ac.gov.br.sigei_backend.utils.Vinculos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
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
    RegistroAnualRepository registroRepository;

    @Autowired
    VinculoSedeService vinculoSedeService;

    @Autowired
    LotacaoProfessorRepository lotacaoProfessorRepository;

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

    @Transactional
    public EscolaResponseDTO salvar(EscolaRequestDTO escolaRequestDTO) {
        Aldeia aldeia = aldeiaRepository.findById(escolaRequestDTO.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        Escola escola = escolaMapper.converter(escolaRequestDTO);

        escola.setAldeia(aldeia);
        vinculoSedeService.vincular(escola, escolaRequestDTO.tipoUnidade(), escolaRequestDTO.escolaSedeId());

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

        RegistroAnual registro = registroRepository.findTopByEscolaOrderByAnoReferencia_AnoDesc(escola)
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

                aldeiaMapper.converterResumo(
                        escola.getAldeia()
                ),

                registro == null
                        ? null
                        : registroAnualMapper.converter(registro)
        );
    }

    @Transactional
    public EscolaResponseDTO editar(Long id, EscolaRequestDTO escolaDTO){
        Escola escola = escolaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        Aldeia aldeia = aldeiaRepository.findById(escolaDTO.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        escolaMapper.atualizar(escolaDTO, escola);

        escola.setAldeia(aldeia);
        vinculoSedeService.vincular(escola, escolaDTO.tipoUnidade(), escolaDTO.escolaSedeId());

        Escola escolaSalva = escolaRepository.save(escola);

        return escolaMapper.converter(escolaSalva);
    }

    @Transactional
    public void excluir(Long id){
        escolaRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Id de escola inválido ou não encontrado"));

        Vinculos.para("Não é possível excluir a escola: ela tem")
                .com(escolaRepository.countByEscolaSede_Id(id), "escola anexa vinculada", "escolas anexas vinculadas")
                .verificar();

        // As lotações de professores na escola saem junto; os professores continuam cadastrados
        lotacaoProfessorRepository.deleteAll(lotacaoProfessorRepository.findByEscola_Id(id));

        escolaRepository.deleteById(id);
    }
}
