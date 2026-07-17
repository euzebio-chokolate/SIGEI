package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {
    Optional<Anexo> findById(Long id);
}
