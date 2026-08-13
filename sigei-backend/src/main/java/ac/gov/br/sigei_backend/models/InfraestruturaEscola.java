package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

@Entity
public class InfraestruturaEscola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "registro_anual_id")
    private RegistroAnual registroAnual;

    private int anoConstrucao;
    private int numSalas;
    private Double latitude;
    private Double longitude;
    private String situacao;
    private String tipoAcesso;
    private Boolean temPredio;
    private String tipoAgua;
    private Boolean temInternet;
    private String tipoEnergia;
    private Boolean temRegistroFotografico;

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

    public String getTipoAgua() {
        return tipoAgua;
    }

    public void setTipoAgua(String tipoAgua) {
        this.tipoAgua = tipoAgua;
    }

    public Boolean getTemInternet() {
        return temInternet;
    }

    public void setTemInternet(Boolean temInternet) {
        this.temInternet = temInternet;
    }

    public String getTipoEnergia() {
        return tipoEnergia;
    }

    public void setTipoEnergia(String tipoEnergia) {
        this.tipoEnergia = tipoEnergia;
    }

    public Boolean getTemRegistroFotografico() {
        return temRegistroFotografico;
    }

    public void setTemRegistroFotografico(Boolean temRegistroFotografico) {
        this.temRegistroFotografico = temRegistroFotografico;
    }
}