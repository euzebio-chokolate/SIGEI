package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"escola_id", "escola_anexo_id"}
        )
)
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;

    @ManyToOne
    @JoinColumn(name = "escola_anexo_id")
    private Escola escolaAnexo;

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

    public Escola getEscolaAnexo() {
        return escolaAnexo;
    }

    public void setEscolaAnexo(Escola escolaAnexo) {
        this.escolaAnexo = escolaAnexo;
    }
}