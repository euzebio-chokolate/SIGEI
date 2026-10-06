package ac.gov.br.sigei_backend.models;

import ac.gov.br.sigei_backend.config.auditoria.RevisaoListener;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.Instant;

/**
 * Revisão da trilha de auditoria (Hibernate Envers): cada transação que altera
 * entidades auditadas gera uma revisão com data, hora e autor. Os valores de
 * cada versão ficam nas tabelas {@code <entidade>_aud}.
 *
 * <p>O autor é copiado do usuário autenticado, sem chave estrangeira, para que a
 * trilha sobreviva à exclusão ou renomeação do usuário. Usuário nulo indica uma
 * alteração feita pelo sistema (carga inicial, inicializadores, migrações).
 */
@Entity
@Table(name = "revisao")
@RevisionEntity(RevisaoListener.class)
public class Revisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    @Column(nullable = false)
    private Instant dataHora;

    private Long usuarioId;

    private String usuarioEmail;

    private String usuarioNome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }
}
