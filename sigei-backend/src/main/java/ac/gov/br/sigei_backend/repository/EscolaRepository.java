package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EscolaRepository extends JpaRepository<Escola, Long> {
    public Optional<Escola> findById(Long id);
    public Optional<Escola> findByLatitudeAndLongitude(Double latitude, Double longitude);
}
