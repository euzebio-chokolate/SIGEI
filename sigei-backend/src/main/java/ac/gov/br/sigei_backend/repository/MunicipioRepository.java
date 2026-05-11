package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    public Optional<Municipio> findById(Long id);
}
