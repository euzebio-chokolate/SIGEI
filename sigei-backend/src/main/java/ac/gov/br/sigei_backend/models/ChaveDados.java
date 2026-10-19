package ac.gov.br.sigei_backend.models;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Chave de dados dos dados sensíveis (CPF, matrícula e número do contrato),
 * guardada cifrada: nunca em claro. Há um único registro (id 1).
 *
 * <p>A chave de dados cifra os valores; ela própria é cifrada pela chave mestra
 * ({@code SIGEI_CHAVE_DADOS_SENSIVEIS}) e, se configurada, também pela chave
 * pública de recuperação. Trocar a chave mestra só recifra este registro.
 * Gerenciada por {@code GerenciadorChaveDados}; não é auditada.
 */
@Entity
@Table(name = "chave_dados")
public class ChaveDados {

    public static final short ID = 1;

    @Id
    private Short id;

    /** Chave de dados cifrada com a chave mestra (AES-256-GCM). */
    @Column(nullable = false, length = 255)
    private String cifradaMestra;

    /** Chave de dados cifrada com a chave pública de recuperação (RSA-OAEP). */
    @Column(length = 1024)
    private String cifradaRecuperacao;

    /** SHA-256 da chave pública de recuperação usada em {@link #cifradaRecuperacao}. */
    @Column(length = 64)
    private String impressaoRecuperacao;

    @Column(nullable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant atualizadaEm;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getCifradaMestra() {
        return cifradaMestra;
    }

    public void setCifradaMestra(String cifradaMestra) {
        this.cifradaMestra = cifradaMestra;
    }

    public String getCifradaRecuperacao() {
        return cifradaRecuperacao;
    }

    public void setCifradaRecuperacao(String cifradaRecuperacao) {
        this.cifradaRecuperacao = cifradaRecuperacao;
    }

    public String getImpressaoRecuperacao() {
        return impressaoRecuperacao;
    }

    public void setImpressaoRecuperacao(String impressaoRecuperacao) {
        this.impressaoRecuperacao = impressaoRecuperacao;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Instant criadaEm) {
        this.criadaEm = criadaEm;
    }

    public Instant getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(Instant atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}
