package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.utils.MunicipioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MunicipioService {
    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    MunicipioMapper municipioMapper;

    public MunicipioResponseDTO salvar(MunicipioRequestDTO municipioRequestDTO) throws DataIntegrityViolationException{
        Municipio municipio = municipioMapper.converter(municipioRequestDTO);
        municipioRepository.save(municipio);

        return municipioMapper.converter(municipio);
    }

    public List<MunicipioResponseDTO> buscarTodos(){
        List<Municipio> municipios = municipioRepository.findAll();
        List<MunicipioResponseDTO> municipioResponseDTOS = new ArrayList<>();
        for (Municipio municipio : municipios){
            MunicipioResponseDTO municipioResponseDTO = municipioMapper.converter(municipio);
            municipioResponseDTOS.add(municipioResponseDTO);
        }

        return municipioResponseDTOS;
    }

    public MunicipioResponseDTO editar(MunicipioRequestDTO municipioDTO){
        municipioRepository.findById(municipioDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Id de municipio inválido ou não encontrado"));

        Municipio municipio = municipioMapper.converter(municipioDTO);
        municipioRepository.save(municipio);
        return municipioMapper.converter(municipio);
    }

    public void excluir(Long id){
        Municipio municipio = municipioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de municipio inválido ou não encontrado"));
        municipioRepository.deleteById(id);
    }

}
