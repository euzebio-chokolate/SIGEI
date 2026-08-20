package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.mappers.MunicipioMapper;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MunicipioService {
    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    EscolaRepository escolaRepository;

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

    public List<MunicipioEstatisticaResponseDTO> buscarEstatisticas() {

        List<Municipio> municipios = municipioRepository.findAll();

        List<MunicipioEstatisticaResponseDTO> resultado =
                new ArrayList<>();

        for (Municipio municipio : municipios) {

            Long quantidadeTerras =
                    terraIndigenaRepository
                            .countByMunicipio_Id(municipio.getId());

            Long quantidadeAldeias =
                    aldeiaRepository
                            .countByTerraIndigena_Municipio_Id(
                                    municipio.getId()
                            );

            Long quantidadeEscolas =
                    escolaRepository
                            .countByAldeia_TerraIndigena_Municipio_Id(
                                    municipio.getId()
                            );

            MunicipioEstatisticaResponseDTO dto =
                    new MunicipioEstatisticaResponseDTO(
                            municipio.getId(),
                            municipio.getNome(),
                            quantidadeTerras,
                            quantidadeAldeias,
                            quantidadeEscolas
                    );

            resultado.add(dto);
        }

        return resultado;
    }

    public void excluir(Long id){
        Municipio municipio = municipioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de municipio inválido ou não encontrado"));
        municipioRepository.deleteById(id);
    }

}
