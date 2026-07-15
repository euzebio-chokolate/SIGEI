package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
public class RegistroAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;

    private Integer ano;
    private String rede;
    private String localFuncionamento;

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

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
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
}