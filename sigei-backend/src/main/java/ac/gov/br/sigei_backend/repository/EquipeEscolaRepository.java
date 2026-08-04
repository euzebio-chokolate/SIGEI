package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipeEscolaRepository extends JpaRepository<EquipeEscola, Long> {
    Optional<EquipeEscola> findById(Long id);
    Optional<EquipeEscola> findTopByRegistroAnualEscolaOrderByRegistroAnualAnoReferenciaAnoDesc(Escola escola);
}
