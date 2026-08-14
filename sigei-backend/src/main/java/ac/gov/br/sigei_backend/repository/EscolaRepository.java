package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscolaRepository extends JpaRepository<Escola, Long> {
    List<Escola> findAllByCodigo(String codigo);
    Optional<Escola> findFirstByCodigoAndTipoUnidadeIgnoreCase(String codigo, String tipoUnidade);
    Optional<Escola> findFirstByCodigoAndNomeIgnoreCase(String codigo, String nome);
    boolean existsByCodigoAndNomeIgnoreCase(String codigo, String nome);
}
