package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistroAnualRepository extends JpaRepository<RegistroAnual, Long> {
    Optional<RegistroAnual> findById(Long id);
    Optional<RegistroAnual> findTopByEscolaOrderByAnoDesc(Escola escola);
}
