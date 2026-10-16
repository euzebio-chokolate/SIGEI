package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.LotacaoProfessorRepository;
import ac.gov.br.sigei_backend.utils.Vinculos;
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
    LotacaoProfessorRepository lotacaoProfessorRepository;

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

    public MunicipioResponseDTO editar(Long id, MunicipioRequestDTO municipioDTO){
        municipioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de municipio inválido ou não encontrado"));

        Municipio municipio = municipioMapper.converter(municipioDTO);
        municipio.setId(id);
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

        Vinculos.para("Não é possível excluir o município: ele tem")
                .com(terraIndigenaRepository.countByMunicipio_Id(id),
                        "terra indígena vinculada", "terras indígenas vinculadas")
                .com(lotacaoProfessorRepository.countByMunicipio_Id(id),
                        "lotação de professor", "lotações de professores")
                .verificar();

        municipioRepository.deleteById(id);
    }

}
