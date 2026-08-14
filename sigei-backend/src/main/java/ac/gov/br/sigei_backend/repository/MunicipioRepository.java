package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    public Optional<Municipio> findById(Long id);
    public Optional<Municipio> findByNome(String name);
    Optional<Municipio> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
