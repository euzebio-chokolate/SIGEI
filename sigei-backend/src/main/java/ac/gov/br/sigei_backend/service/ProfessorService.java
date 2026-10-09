package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.config.criptografia.CriptografiaDadosSensiveis;
import ac.gov.br.sigei_backend.config.security.AcessoDadosSensiveis;
import ac.gov.br.sigei_backend.dto.request.ProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.ProfessorResponseDTO;
import ac.gov.br.sigei_backend.mappers.ProfessorMapper;
import ac.gov.br.sigei_backend.models.Professor;
import ac.gov.br.sigei_backend.repository.ProfessorRepository;
import ac.gov.br.sigei_backend.utils.CpfUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ProfessorService {

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    ProfessorMapper professorMapper;

    @Autowired
    CriptografiaDadosSensiveis criptografia;

    @Autowired
    AcessoDadosSensiveis acessoDadosSensiveis;

    @Transactional
    public ProfessorResponseDTO salvar(ProfessorRequestDTO professorDTO) {
        Professor professor = new Professor();
        preencher(professorDTO, professor);

        return converter(professorRepository.save(professor));
    }

    public List<ProfessorResponseDTO> buscarTudo() {
        return professorRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Professor::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::converter)
                .toList();
    }

    public ProfessorResponseDTO buscarPorId(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado com ID fornecido"));

        return converter(professor);
    }

    @Transactional
    public ProfessorResponseDTO editar(ProfessorRequestDTO professorDTO) {
        if (professorDTO.id() == null) {
            throw new IllegalArgumentException("Informe o ID do professor");
        }

        Professor professor = professorRepository.findById(professorDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado"));

        preencher(professorDTO, professor);

        return converter(professorRepository.save(professor));
    }

    /** Exclui o professor e, em cascata, as suas lotações. */
    @Transactional
    public void excluir(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id de professor inválido ou não encontrado"));

        professorRepository.delete(professor);
    }

    /**
     * Grava o CPF só com os dígitos e atualiza o índice cego usado na unicidade.
     * Também usado pela importação da planilha.
     */
    public void definirCpf(Professor professor, String cpf) {
        String normalizado = CpfUtils.normalizar(cpf);
        String indice = criptografia.indice(normalizado);

        if (indice != null) {
            boolean duplicado = professor.getId() == null
                    ? professorRepository.existsByCpfHash(indice)
                    : professorRepository.existsByCpfHashAndIdNot(indice, professor.getId());

            if (duplicado) {
                throw new IllegalArgumentException("CPF já cadastrado para outro professor.");
            }
        }

        professor.setCpf(normalizado);
        professor.setCpfHash(indice);
    }

    private void preencher(ProfessorRequestDTO professorDTO, Professor professor) {
        professorMapper.atualizar(professorDTO, professor);

        professor.setNome(professorDTO.nome().trim());
        professor.setMatricula(textoOuNulo(professorDTO.matricula()));
        professor.setEtnia(textoOuNulo(professorDTO.etnia()));
        professor.setFormacao(textoOuNulo(professorDTO.formacao()));
        professor.setSexo(textoOuNulo(professorDTO.sexo()));

        definirCpf(professor, professorDTO.cpf());
    }

    private ProfessorResponseDTO converter(Professor professor) {
        ProfessorResponseDTO resposta = professorMapper.converter(professor);
        return acessoDadosSensiveis.podeVer() ? resposta : resposta.semDadosSensiveis();
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
