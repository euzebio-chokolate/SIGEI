package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AldeiaRepository extends JpaRepository<Aldeia, Long> {
    public Optional<Aldeia> findById(Long id);
}
