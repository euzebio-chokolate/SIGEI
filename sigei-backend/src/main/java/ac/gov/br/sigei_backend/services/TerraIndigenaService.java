package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.TerraIndigenaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TerraIndigenaService {
    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

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
