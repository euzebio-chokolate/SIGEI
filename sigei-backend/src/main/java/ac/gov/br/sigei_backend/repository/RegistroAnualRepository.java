package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistroAnualRepository extends JpaRepository<RegistroAnual, Long> {
    Optional<RegistroAnual> findById(Long id);
    //Optional<RegistroAnual> findTopByEscolaOrderByAnoReferenciaAnoDesc(Escola escola);
    boolean existsByEscolaAndAnoReferencia(Escola escola, AnoReferencia anoReferencia);
    Optional<RegistroAnual> findTopByEscolaOrderByAnoReferencia_AnoDesc(Escola escola);
    Optional<RegistroAnual> findByEscolaAndAnoReferencia(Escola escola, AnoReferencia anoReferencia);
}
