package ac.gov.br.sigei_backend.models;

import ac.gov.br.sigei_backend.config.criptografia.DadoSensivelConverter;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

/**
 * Lotação de um professor em um ano: onde atua, em qual cargo e segmento, e o
 * contrato. Sem professor, representa uma vaga em aberto (a "NECESSIDADE" da
 * planilha). Sem escola, a lotação é no núcleo de educação do município (o
 * "NEE SEE" da planilha). O município é gravado sempre; com escola, é o dela.
 * O número do contrato é sensível: fica cifrado e só é devolvido ao perfil ADMIN.
 */
@Entity
@Audited
@Table(name = "lotacao_professor")
public class LotacaoProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ano_referencia_id", nullable = false)
    private AnoReferencia anoReferencia;

    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;

    @ManyToOne(optional = false)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipio;

    private String cargo;

    private String segmento;

    @Convert(converter = DadoSensivelConverter.class)
    private String numeroContrato;

    private LocalDate inicioContrato;

    private String amparoLegal;

    private String tipoContrato;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public AnoReferencia getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(AnoReferencia anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public Escola getEscola() {
        return escola;
    }

    public void setEscola(Escola escola) {
        this.escola = escola;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public LocalDate getInicioContrato() {
        return inicioContrato;
    }

    public void setInicioContrato(LocalDate inicioContrato) {
        this.inicioContrato = inicioContrato;
    }

    public String getAmparoLegal() {
        return amparoLegal;
    }

    public void setAmparoLegal(String amparoLegal) {
        this.amparoLegal = amparoLegal;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }
}
