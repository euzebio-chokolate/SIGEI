package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AldeiaRepository extends JpaRepository<Aldeia, Long> {
    public Optional<Aldeia> findById(Long id);
    Optional<Aldeia> findByNomeIgnoreCaseAndTerraIndigenaAndPovo(String nome, TerraIndigena terraIndigena, Povo povo);
    boolean existsByNomeIgnoreCaseAndTerraIndigenaAndPovo(String nome, TerraIndigena terraIndigena, Povo povo);
    long countByTerraIndigena_Municipio_Id(Long municipioId);
    long countByTerraIndigena_Id(Long terraIndigenaId);
}
