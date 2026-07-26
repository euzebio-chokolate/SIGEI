package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
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
}
