package ac.gov.br.sigei_backend.models;

import ac.gov.br.sigei_backend.config.criptografia.DadoSensivelConverter;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.ArrayList;
import java.util.List;

/**
 * Dados pessoais do professor. CPF e matrícula são sensíveis: ficam cifrados no
 * banco (também nas tabelas de auditoria) e só são devolvidos ao perfil ADMIN.
 * O {@code cpfHash} é o índice cego do CPF, usado para unicidade e busca.
 */
@Entity
@Audited
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_professor_cpf_hash",
                        columnNames = {"cpf_hash"}
                )
        }
)
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Convert(converter = DadoSensivelConverter.class)
    private String cpf;

    @NotAudited
    @Column(name = "cpf_hash", length = 64)
    private String cpfHash;

    @Convert(converter = DadoSensivelConverter.class)
    private String matricula;

    private String etnia;

    @Column(length = 1)
    private String sexo;

    private String formacao;

    // Excluir o professor exclui suas lotações. A lotação é o lado dono do vínculo:
    // trocar o professor de uma lotação não exige alterar esta coleção.
    @OneToMany(
            mappedBy = "professor",
            cascade = CascadeType.REMOVE
    )
    private List<LotacaoProfessor> lotacoes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public void setCpfHash(String cpfHash) {
        this.cpfHash = cpfHash;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEtnia() {
        return etnia;
    }

    public void setEtnia(String etnia) {
        this.etnia = etnia;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getFormacao() {
        return formacao;
    }

    public void setFormacao(String formacao) {
        this.formacao = formacao;
    }

    public List<LotacaoProfessor> getLotacoes() {
        return lotacoes;
    }

    public void setLotacoes(List<LotacaoProfessor> lotacoes) {
        this.lotacoes = lotacoes;
    }
}
