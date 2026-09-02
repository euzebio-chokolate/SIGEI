package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Povo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PovoRepository extends JpaRepository<Povo, Long> {
    @Override
    @EntityGraph(attributePaths = "aldeias")
    List<Povo> findAll();

    @Override
    @EntityGraph(attributePaths = "aldeias")
    Optional<Povo> findById(Long id);

    Optional<Povo> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
