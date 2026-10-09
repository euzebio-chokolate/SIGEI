package ac.gov.br.sigei_backend.dto.response;

/**
 * cpf e matricula só são preenchidos para o perfil ADMIN; para os demais, vêm nulos.
 */
public record ProfessorResponseDTO(

        Long id,

        String nome,

        String cpf,

        String matricula,

        String etnia,

        String sexo,

        String formacao

) {

    public ProfessorResponseDTO semDadosSensiveis() {
        return new ProfessorResponseDTO(id, nome, null, null, etnia, sexo, formacao);
    }
}
