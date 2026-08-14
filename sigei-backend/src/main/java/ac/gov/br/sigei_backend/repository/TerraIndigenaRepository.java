package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerraIndigenaRepository extends JpaRepository<TerraIndigena, Long> {
    public Optional<TerraIndigena> findById(Long id);
    Optional<TerraIndigena>
    findByNomeIgnoreCaseAndMunicipio(String nome, Municipio municipio);
    boolean existsByNomeIgnoreCaseAndMunicipio(String nome, Municipio municipio);
}
