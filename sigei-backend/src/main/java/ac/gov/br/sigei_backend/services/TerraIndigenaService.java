package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
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
        Optional<Municipio> optionalMunicipio = municipioRepository.findById(terraIndigenaDTO.municipioId());
        if (optionalMunicipio.isEmpty()){
            throw new NoSuchElementException("Id de municipio inválido ou não encontrado");
        }

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome(terraIndigenaDTO.nome());
        terraIndigena.setMunicipio(optionalMunicipio.get());

        TerraIndigena terraIndigenaSalva = terraIndigenaRepository.save(terraIndigena);
        return terraIndigenaMapper.converter(terraIndigenaSalva);
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
        Optional<TerraIndigena> optionalTerraIndigena = terraIndigenaRepository.findById(terraIndigenaDTO.id());
        if (optionalTerraIndigena.isEmpty()){
            throw new NoSuchElementException("Id de terra indígena inválido ou não encontrado");
        }

        Optional<Municipio> optionalMunicipio = municipioRepository.findById(terraIndigenaDTO.municipioId());
        if (optionalMunicipio.isEmpty()){
            throw new NoSuchElementException("Id de município inválido ou não encontrado");
        }

        TerraIndigena terraIndigena = optionalTerraIndigena.get();
        Municipio municipio = optionalMunicipio.get();

        terraIndigena.setNome(terraIndigenaDTO.nome());
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);

        TerraIndigena terraIndigenaSalva = terraIndigenaRepository.save(terraIndigena);
        return terraIndigenaMapper.converter(terraIndigenaSalva);
    }

    public void excluir(Long id){
        Optional<TerraIndigena> optionalTerraIndigena = terraIndigenaRepository.findById(id);
        terraIndigenaRepository.deleteById(id);
    }
}
