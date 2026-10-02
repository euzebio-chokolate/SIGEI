package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.RefreshToken;
import ac.gov.br.sigei_backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revoga o token apenas se ainda estiver ativo; retorna 0 se outra requisição já o consumiu. */
    @Modifying
    @Query("update RefreshToken r set r.revogado = true, r.revogadoEm = :agora where r.id = :id and r.revogado = false")
    int revogarSeAtivo(@Param("id") Long id, @Param("agora") Instant agora);

    @Modifying
    @Query("update RefreshToken r set r.revogado = true, r.revogadoEm = :agora where r.usuario = :usuario and r.revogado = false")
    int revogarTodosDoUsuario(@Param("usuario") Usuario usuario, @Param("agora") Instant agora);

    @Modifying
    @Query("delete from RefreshToken r where r.usuario = :usuario and r.expiraEm < :limite")
    int excluirExpiradosDoUsuario(@Param("usuario") Usuario usuario, @Param("limite") Instant limite);
}
