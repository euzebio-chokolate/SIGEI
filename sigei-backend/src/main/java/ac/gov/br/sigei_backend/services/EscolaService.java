package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.EscolaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    EscolaMapper escolaMapper;

    public EscolaResponseDTO salvar(EscolaRequestDTO escolaRequestDTO) throws DataIntegrityViolationException{
        Municipio municipio = municipioRepository.findById(escolaRequestDTO.municipioId())
                .orElseThrow(() -> new NoSuchElementException("Municipio não encontrado"));

        TerraIndigena terraIndigena = terraIndigenaRepository.findById(escolaRequestDTO.terraIndigenaId())
                .orElseThrow(() -> new NoSuchElementException("Terra índigena não encontrada"));

        Aldeia aldeia = aldeiaRepository.findById(escolaRequestDTO.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        Escola escolaSede = escolaRequestDTO.escolaSedeId() == null
                ? null
                : escolaRepository.findById(escolaRequestDTO.escolaSedeId())
                .orElseThrow(() -> new NoSuchElementException("Escola sede não encontrada"));

        Escola escola = escolaMapper.converter(escolaRequestDTO);

        escola.setMunicipio(municipio);
        escola.setTerraIndigena(terraIndigena);
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

    public EscolaResponseDTO editar(EscolaRequestDTO escolaDTO){
        Escola escola = escolaRepository.findById(escolaDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Escola não encontrada"));

        Municipio municipio = municipioRepository.findById(escolaDTO.municipioId())
                .orElseThrow(() -> new NoSuchElementException("Município não encontrado"));

        TerraIndigena terraIndigena = terraIndigenaRepository.findById(escolaDTO.terraIndigenaId())
                .orElseThrow(() -> new NoSuchElementException("Terra indígena não encontrada"));

        Aldeia aldeia = aldeiaRepository.findById(escolaDTO.aldeiaId())
                .orElseThrow(() -> new NoSuchElementException("Aldeia não encontrada"));

        Escola escolaSede = escolaDTO.escolaSedeId() == null
                ? null
                : escolaRepository.findById(escolaDTO.escolaSedeId())
                .orElseThrow(() -> new NoSuchElementException("Escola sede não encontrada"));

        escolaMapper.atualizar(escolaDTO, escola);

        escola.setMunicipio(municipio);
        escola.setTerraIndigena(terraIndigena);
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
