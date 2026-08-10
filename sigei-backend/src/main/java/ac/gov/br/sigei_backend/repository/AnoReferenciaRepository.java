package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnoReferenciaRepository extends JpaRepository<AnoReferencia, Long> {

    Optional<AnoReferencia> findByAno(Integer ano);
    Optional<AnoReferencia> findTopByOrderByAnoDesc();

}