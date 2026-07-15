package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
public class InfraestruturaEscola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;

    private int anoReferencia;
    private int anoConstrucao;
    private int numSalas;
    private Double latitude;
    private Double longitude;
    private String situacao;
    private String tipoAcesso;
    private Boolean temPredio;
    private Boolean temAgua;
    private Boolean temInternet;
    private Boolean temEnergia;
    private Boolean temRegistroFotografico;

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

    public int getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(int anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public int getAnoConstrucao() {
        return anoConstrucao;
    }

    public void setAnoConstrucao(int anoConstrucao) {
        this.anoConstrucao = anoConstrucao;
    }

    public int getNumSalas() {
        return numSalas;
    }

    public void setNumSalas(int numSalas) {
        this.numSalas = numSalas;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(String tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }

    public Boolean getTemPredio() {
        return temPredio;
    }

    public void setTemPredio(Boolean temPredio) {
        this.temPredio = temPredio;
    }

    public Boolean getTemAgua() {
        return temAgua;
    }

    public void setTemAgua(Boolean temAgua) {
        this.temAgua = temAgua;
    }

    public Boolean getTemInternet() {
        return temInternet;
    }

    public void setTemInternet(Boolean temInternet) {
        this.temInternet = temInternet;
    }

    public Boolean getTemEnergia() {
        return temEnergia;
    }

    public void setTemEnergia(Boolean temEnergia) {
        this.temEnergia = temEnergia;
    }

    public Boolean getTemRegistroFotografico() {
        return temRegistroFotografico;
    }

    public void setTemRegistroFotografico(Boolean temRegistroFotografico) {
        this.temRegistroFotografico = temRegistroFotografico;
    }
}