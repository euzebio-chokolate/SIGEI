package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.MatriculaNivel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatriculaNivelRepository extends JpaRepository<MatriculaNivel, Long> {
    Optional<MatriculaNivel> findById(Long id);
}
