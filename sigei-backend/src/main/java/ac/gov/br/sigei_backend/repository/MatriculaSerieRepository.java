package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.MatriculaSerie;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaSerieRepository extends JpaRepository<MatriculaSerie, Long> {
    List<MatriculaSerie> findByRegistro(RegistroAnual registro);
    void deleteByRegistro(RegistroAnual registro);
}