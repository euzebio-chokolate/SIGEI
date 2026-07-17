package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MatriculaSerieRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MatriculaSerieResponseDTO;
import ac.gov.br.sigei_backend.mappers.MatriculaSerieMapper;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.MatriculaSerieRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MatriculaSerieService {
    @Autowired
    RegistroAnualRepository registroRepository;

    @Autowired
    MatriculaSerieRepository matriculaSerieRepository;

    @Autowired
    MatriculaSerieMapper matriculaSerieMapper;

    public MatriculaSerieResponseDTO salvar(MatriculaSerieRequestDTO matriculaSerieDTO){
        RegistroAnual registro = registroRepository.findById(matriculaSerieDTO.registroId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        MatriculaSerie matriculaSerie = matriculaSerieMapper.converter(matriculaSerieDTO);
        matriculaSerie.setRegistro(registro);
        MatriculaSerie matriculaSerieSalva = matriculaSerieRepository.save(matriculaSerie);

        return matriculaSerieMapper.converter(matriculaSerieSalva);
    }

    public List<MatriculaSerieResponseDTO> buscarTudo(){
        return matriculaSerieRepository.findAll().stream().map(matriculaSerieMapper::converter).toList();
    }

    public MatriculaSerieResponseDTO editar(MatriculaSerieRequestDTO matriculaSerieDTO){

        MatriculaSerie matriculaSerie = matriculaSerieRepository.findById(matriculaSerieDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada"));

        RegistroAnual registroAnual = registroRepository.findById(matriculaSerieDTO.registroId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        matriculaSerieMapper.atualizar(matriculaSerieDTO, matriculaSerie);
        matriculaSerie.setRegistro(registroAnual);
        MatriculaSerie matriculaSerieSalva = matriculaSerieRepository.save(matriculaSerie);
        return matriculaSerieMapper.converter(matriculaSerieSalva);
    }

    public void excluir(Long id) {
        matriculaSerieRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada"));

        matriculaSerieRepository.deleteById(id);
    }
}
