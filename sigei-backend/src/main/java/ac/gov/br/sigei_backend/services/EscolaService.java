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
        Optional<Municipio> optionalMunicipio = municipioRepository.findById(escolaRequestDTO.municipioId());
        if (optionalMunicipio.isEmpty()){
            throw new RuntimeException("Município não encontrado");
        }

        Optional<TerraIndigena> optionalTerraIndigena = terraIndigenaRepository.findById(escolaRequestDTO.terraIndigenaId());
        if (optionalTerraIndigena.isEmpty()){
            throw new RuntimeException("Terra indígena não encontrada");
        }

        Optional<Aldeia> optionalAldeia = aldeiaRepository.findById(escolaRequestDTO.aldeiaId());
        if (optionalAldeia.isEmpty()){
            throw new RuntimeException("Aldeia não encontrada");
        }

        Escola escola = new Escola();

        escola.setCodigo(escolaRequestDTO.codigo());
        escola.setNome(escolaRequestDTO.nome());

        escola.setMunicipio(optionalMunicipio.get());
        escola.setTerraIndigena(optionalTerraIndigena.get());
        escola.setAldeia(optionalAldeia.get());

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
        Optional<Escola> optionalEscola = escolaRepository.findById(escolaDTO.id());
        if (optionalEscola.isEmpty()){
            throw new NoSuchElementException("Id de escola inválido ou não encontrado");
        }

        Optional<>
    }

}
