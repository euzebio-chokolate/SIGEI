package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.ChaveDados;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChaveDadosRepository extends JpaRepository<ChaveDados, Short> {
}
