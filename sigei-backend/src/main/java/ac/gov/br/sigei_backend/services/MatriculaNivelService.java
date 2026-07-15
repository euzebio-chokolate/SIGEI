package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MatriculaNivelRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MatriculaNivelResponseDTO;
import ac.gov.br.sigei_backend.mappers.MatriculaNivelMapper;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.MatriculaNivelRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MatriculaNivelService {

    @Autowired
    RegistroAnualRepository registroRepository;

    @Autowired
    MatriculaNivelRepository matriculaRepository;

    @Autowired
    MatriculaNivelMapper matriculaMapper;

    public MatriculaNivelResponseDTO salvar(MatriculaNivelRequestDTO dto) {

        RegistroAnual registro = registroRepository.findById(dto.registroId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        MatriculaNivel matricula = matriculaMapper.converter(dto);
        matricula.setRegistro(registro);
        MatriculaNivel matriculaSalva = matriculaRepository.save(matricula);

        return matriculaMapper.converter(matriculaSalva);
    }

    public List<MatriculaNivelResponseDTO> buscarTudo() {
        return matriculaRepository.findAll().stream().map(matriculaMapper::converter).toList();
    }

    public MatriculaNivelResponseDTO editar(MatriculaNivelRequestDTO dto) {

        MatriculaNivel matricula = matriculaRepository.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada"));

        RegistroAnual registro = registroRepository.findById(dto.registroId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        matriculaMapper.atualizar(dto, matricula);
        matricula.setRegistro(registro);
        MatriculaNivel matriculaSalva = matriculaRepository.save(matricula);
        return matriculaMapper.converter(matriculaSalva);
    }

    public void excluir(Long id) {
        matriculaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada"));

        matriculaRepository.deleteById(id);
    }
}
