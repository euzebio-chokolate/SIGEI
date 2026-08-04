package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"escola_id", "ano_referencia"}
        )
)
public class EquipeEscola {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "registro_anual_id")
    private RegistroAnual registroAnual;

    @ManyToOne
    @JoinColumn(name = "ano_referencia_id")
    private AnoReferencia anoReferencia;

    private boolean temProfessorResponsavel;
    private boolean temEquipeGestora;
    private boolean temMerendeira;
    private boolean temZeladora;
    private boolean temBarqueiro;
    private boolean necessitaTransporte;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistroAnual getRegistroAnual() {
        return registroAnual;
    }

    public void setRegistroAnual(RegistroAnual registroAnual) {
        this.registroAnual = registroAnual;
    }

    public AnoReferencia getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(AnoReferencia anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public boolean isTemProfessorResponsavel() {
        return temProfessorResponsavel;
    }

    public void setTemProfessorResponsavel(boolean temProfessorResponsavel) {
        this.temProfessorResponsavel = temProfessorResponsavel;
    }

    public boolean isTemEquipeGestora() {
        return temEquipeGestora;
    }

    public void setTemEquipeGestora(boolean temEquipeGestora) {
        this.temEquipeGestora = temEquipeGestora;
    }

    public boolean isTemMerendeira() {
        return temMerendeira;
    }

    public void setTemMerendeira(boolean temMerendeira) {
        this.temMerendeira = temMerendeira;
    }

    public boolean isTemZeladora() {
        return temZeladora;
    }

    public void setTemZeladora(boolean temZeladora) {
        this.temZeladora = temZeladora;
    }

    public boolean isTemBarqueiro() {
        return temBarqueiro;
    }

    public void setTemBarqueiro(boolean temBarqueiro) {
        this.temBarqueiro = temBarqueiro;
    }

    public boolean isNecessitaTransporte() {
        return necessitaTransporte;
    }

    public void setNecessitaTransporte(boolean necessitaTransporte) {
        this.necessitaTransporte = necessitaTransporte;
    }
}
