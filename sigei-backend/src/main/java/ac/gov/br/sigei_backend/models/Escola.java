package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
public class Escola {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nome;

    @ManyToOne
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @ManyToOne
    @JoinColumn(name = "terra_indigena_id")
    private TerraIndigena terraIndigena;

    @OneToOne
    @JoinColumn(name = "aldeia_id")
    private Aldeia aldeia;

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

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public TerraIndigena getTerraIndigena() {
        return terraIndigena;
    }

    public void setTerraIndigena(TerraIndigena terraIndigena) {
        this.terraIndigena = terraIndigena;
    }

    public Aldeia getAldeia() {
        return aldeia;
    }

    public void setAldeia(Aldeia aldeia) {
        this.aldeia = aldeia;
    }
}
