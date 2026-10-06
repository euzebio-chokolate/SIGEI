package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class MatriculaNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registro_id")
    private RegistroAnual registro;

    private String nivel;
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

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
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