package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.TerraIndigenaEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.mappers.MunicipioMapper;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.mappers.TerraIndigenaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TerraIndigenaService {
    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    MunicipioMapper municipioMapper;

    @Autowired
    TerraIndigenaMapper terraIndigenaMapper;

    @Autowired
    MunicipioRepository municipioRepository;

    public TerraIndigenaResponseDTO salvar(TerraIndigenaRequestDTO terraIndigenaDTO){
        Municipio municipio = municipioRepository.findById(terraIndigenaDTO.municipioId())
                .orElseThrow(() -> new NoSuchElementException("Municipio não encontrado"));

        TerraIndigena terraIndigena = terraIndigenaMapper.converter(terraIndigenaDTO);
        terraIndigena.setMunicipio(municipio);
        TerraIndigena terraSalva = terraIndigenaRepository.save(terraIndigena);
        return terraIndigenaMapper.converter(terraSalva);
    }

    public List<TerraIndigenaResponseDTO> buscarTudo(){
        List<TerraIndigena> terrasIndigenas = terraIndigenaRepository.findAll();
        List<TerraIndigenaResponseDTO> terraIndigenaResponseDTOS = new ArrayList<>();
        for (TerraIndigena terraIndigena : terrasIndigenas){
            TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaMapper.converter(terraIndigena);
            terraIndigenaResponseDTOS.add(terraIndigenaResponseDTO);
        }
        return terraIndigenaResponseDTOS;
    }

    public TerraIndigenaResponseDTO buscarPorId(Long id) {
        TerraIndigena terraIndigena = terraIndigenaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Terra indígena não encontrada com o ID fornecido"));

        return terraIndigenaMapper.converter(terraIndigena);
    }

    public List<TerraIndigenaEstatisticaResponseDTO> buscarEstatisticas() {

        List<TerraIndigena> terrasIndigenas =
                terraIndigenaRepository.findAll();

        List<TerraIndigenaEstatisticaResponseDTO> resultado =
                new ArrayList<>();

        for (TerraIndigena terraIndigena : terrasIndigenas) {

            Long quantidadeAldeias =
                    aldeiaRepository.countByTerraIndigena_Id(
                            terraIndigena.getId()
                    );

            Long quantidadeEscolas =
                    escolaRepository.countByAldeia_TerraIndigena_Id(
                            terraIndigena.getId()
                    );

            TerraIndigenaEstatisticaResponseDTO dto =
                    new TerraIndigenaEstatisticaResponseDTO(

                            terraIndigena.getId(),

                            terraIndigena.getNome(),

                            municipioMapper.converterResumo(
                                    terraIndigena.getMunicipio()
                            ),

                            quantidadeAldeias,

                            quantidadeEscolas
                    );

            resultado.add(dto);
        }

        return resultado;
    }

    public TerraIndigenaResponseDTO editar(TerraIndigenaRequestDTO terraIndigenaDTO){
        terraIndigenaRepository.findById(terraIndigenaDTO.id())
                        .orElseThrow(() -> new NoSuchElementException("Id de terra indígena inválido ou não encontrado"));

        Municipio municipio = municipioRepository.findById(terraIndigenaDTO.municipioId())
                .orElseThrow(() -> new NoSuchElementException("Id de município inválido ou não encontrado"));

        TerraIndigena terraIndigena = terraIndigenaMapper.converter(terraIndigenaDTO);
        terraIndigena.setId(terraIndigenaDTO.id());
        terraIndigena.setMunicipio(municipio);

        TerraIndigena TerraSalva = terraIndigenaRepository.save(terraIndigena);
        return terraIndigenaMapper.converter(TerraSalva);
    }


    public void excluir(Long id){
        terraIndigenaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de terra indígena inválido ou não encontrado"));
        terraIndigenaRepository.deleteById(id);
    }
}
