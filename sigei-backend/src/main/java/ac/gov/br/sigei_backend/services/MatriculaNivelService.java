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
    MatriculaNivelRepository matriculaNivelRepository;

    @Autowired
    MatriculaNivelMapper matriculaNivelMapper;

    public MatriculaNivelResponseDTO salvar(MatriculaNivelRequestDTO matriculaNivelDTO) {

        RegistroAnual registro = registroRepository.findById(matriculaNivelDTO.registroId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        MatriculaNivel matricula = matriculaNivelMapper.converter(matriculaNivelDTO);
        matricula.setRegistro(registro);
        MatriculaNivel matriculaSalva = matriculaNivelRepository.save(matricula);

        return matriculaNivelMapper.converter(matriculaSalva);
    }

    public List<MatriculaNivelResponseDTO> buscarTudo() {
        return matriculaNivelRepository.findAll().stream().map(matriculaNivelMapper::converter).toList();
    }

    public MatriculaNivelResponseDTO editar(MatriculaNivelRequestDTO matriculaNivelDTO) {

        MatriculaNivel matricula = matriculaNivelRepository.findById(matriculaNivelDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada"));

        RegistroAnual registro = registroRepository.findById(matriculaNivelDTO.registroId())
                .orElseThrow(() -> new NoSuchElementException("Registro anual não encontrado"));

        matriculaNivelMapper.atualizar(matriculaNivelDTO, matricula);
        matricula.setRegistro(registro);
        MatriculaNivel matriculaSalva = matriculaNivelRepository.save(matricula);
        return matriculaNivelMapper.converter(matriculaSalva);
    }

    public void excluir(Long id) {
        matriculaNivelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Matrícula não encontrada"));

        matriculaNivelRepository.deleteById(id);
    }
}
