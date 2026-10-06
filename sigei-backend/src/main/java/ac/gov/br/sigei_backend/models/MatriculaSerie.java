package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class MatriculaSerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registro_id")
    private RegistroAnual registro;

    private String denominacao;
    private String etapaEnsino;
    private Integer qtdMasculino;
    private Integer qtdFeminino;
    private Integer total;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistroAnual getRegistro() {
        return registro;
    }

    public void setRegistro(RegistroAnual registro) {
        this.registro = registro;
    }

    public String getDenominacao() {
        return denominacao;
    }

    public void setDenominacao(String denominacao) {
        this.denominacao = denominacao;
    }

    public String getEtapaEnsino() {
        return etapaEnsino;
    }

    public void setEtapaEnsino(String etapaEnsino) {
        this.etapaEnsino = etapaEnsino;
    }

    public Integer getQtdMasculino() {
        return qtdMasculino;
    }

    public void setQtdMasculino(Integer qtdMasculino) {
        this.qtdMasculino = qtdMasculino;
    }

    public Integer getQtdFeminino() {
        return qtdFeminino;
    }

    public void setQtdFeminino(Integer qtdFeminino) {
        this.qtdFeminino = qtdFeminino;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
