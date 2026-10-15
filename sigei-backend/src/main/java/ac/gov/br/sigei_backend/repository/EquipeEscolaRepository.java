package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipeEscolaRepository extends JpaRepository<EquipeEscola, Long> {
    Optional<EquipeEscola> findById(Long id);
    Optional<EquipeEscola> findTopByRegistroAnualEscolaOrderByRegistroAnualAnoReferenciaAnoDesc(Escola escola);
    Optional<EquipeEscola> findByRegistroAnual_Id(Long registroAnualId);
    boolean existsByRegistroAnual_Id(Long registroAnualId);
    boolean existsByRegistroAnual_IdAndIdNot(Long registroAnualId, Long id);
    Optional<EquipeEscola> findTopByRegistroAnual_EscolaOrderByRegistroAnual_AnoReferencia_AnoDesc(Escola escola);
    List<EquipeEscola> findByRegistroAnual_AnoReferencia_Ano(Integer ano);
}
