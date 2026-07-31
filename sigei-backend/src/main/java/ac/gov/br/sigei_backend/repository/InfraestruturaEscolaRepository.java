package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfraestruturaEscolaRepository extends JpaRepository<InfraestruturaEscola, Long> {
    Optional<InfraestruturaEscola> findById(Long id);
    Optional<InfraestruturaEscola> findTopByEscolaOrderByAnoReferenciaDesc(Escola escola);
}
