package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"escola_id", "ano_referencia_id"}
                )
        }
)
public class RegistroAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;

    @ManyToOne
    @JoinColumn(name = "ano_referencia_id")
    private AnoReferencia anoReferencia;

    private String rede;
    private String localFuncionamento;
    private Integer totalAlunos;

    @OneToOne(
            mappedBy = "registroAnual",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private EquipeEscola equipe;

    @OneToOne(
            mappedBy = "registroAnual",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private InfraestruturaEscola infraestrutura;

    @OneToMany(
            mappedBy = "registro",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MatriculaNivel> matriculasNivel = new ArrayList<>();

    @OneToMany(
            mappedBy = "registro",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MatriculaSerie> matriculasSerie = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Escola getEscola() {
        return escola;
    }

    public void setEscola(Escola escola) {
        this.escola = escola;
    }

    public AnoReferencia getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(AnoReferencia anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public String getRede() {
        return rede;
    }

    public void setRede(String rede) {
        this.rede = rede;
    }

    public String getLocalFuncionamento() {
        return localFuncionamento;
    }

    public void setLocalFuncionamento(String localFuncionamento) {
        this.localFuncionamento = localFuncionamento;
    }

    public Integer getTotalAlunos() {
        return totalAlunos;
    }

    public void setTotalAlunos(Integer totalAlunos) {
        this.totalAlunos = totalAlunos;
    }

    public EquipeEscola getEquipe() {
        return equipe;
    }

    public void setEquipe(EquipeEscola equipe) {
        this.equipe = equipe;
    }

    public InfraestruturaEscola getInfraestrutura() {
        return infraestrutura;
    }

    public void setInfraestrutura(InfraestruturaEscola infraestrutura) {
        this.infraestrutura = infraestrutura;
    }

    public List<MatriculaNivel> getMatriculasNivel() {
        return matriculasNivel;
    }

    public void setMatriculasNivel(List<MatriculaNivel> matriculasNivel) {
        this.matriculasNivel = matriculasNivel;
    }

    public List<MatriculaSerie> getMatriculasSerie() {
        return matriculasSerie;
    }

    public void setMatriculasSerie(List<MatriculaSerie> matriculasSerie) {
        this.matriculasSerie = matriculasSerie;
    }
}