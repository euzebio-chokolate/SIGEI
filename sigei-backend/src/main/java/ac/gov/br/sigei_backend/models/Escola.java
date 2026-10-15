package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
public class Escola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String nome;

    private String tipoUnidade;

    @ManyToOne
    @JoinColumn(name = "escola_sede_id")
    private Escola escolaSede;

    // Uma aldeia pode ter mais de uma escola (em Belo Monte, Feijó, há duas sedes)
    @ManyToOne
    @JoinColumn(
            name = "aldeia_id",
            nullable = true
    )
    private Aldeia aldeia;

    @OneToMany(
            mappedBy = "escola",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RegistroAnual> registros = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoUnidade() {
        return tipoUnidade;
    }

    public void setTipoUnidade(String tipoUnidade) {
        this.tipoUnidade = tipoUnidade;
    }

    public Escola getEscolaSede() {
        return escolaSede;
    }

    public void setEscolaSede(Escola escolaSede) {
        this.escolaSede = escolaSede;
    }

    public Aldeia getAldeia() {
        return aldeia;
    }

    public void setAldeia(Aldeia aldeia) {
        this.aldeia = aldeia;
    }

    public List<RegistroAnual> getRegistros() {
        return registros;
    }

    public void setRegistros(List<RegistroAnual> registros) {
        this.registros = registros;
    }
}
