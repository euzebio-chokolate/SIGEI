package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByCpfHash(String cpfHash);
    boolean existsByCpfHashAndIdNot(String cpfHash, Long id);
    boolean existsByCpfHash(String cpfHash);
}
