package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Povo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PovoRepository extends JpaRepository<Povo, Long> {
    public Optional<Povo> findById(Long id);
    Optional<Povo> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
