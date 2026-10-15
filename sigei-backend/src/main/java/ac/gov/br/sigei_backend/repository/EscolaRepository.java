package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Escola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscolaRepository extends JpaRepository<Escola, Long> {
    List<Escola> findAllByCodigo(String codigo);
    Optional<Escola> findFirstByCodigoAndTipoUnidadeIgnoreCase(String codigo, String tipoUnidade);
    Optional<Escola> findFirstByCodigoAndNomeIgnoreCase(String codigo, String nome);
    Optional<Escola> findFirstByCodigoAndAldeia_NomeIgnoreCase(String codigo, String nomeAldeia);
    boolean existsByCodigoAndNomeIgnoreCase(String codigo, String nome);
    boolean existsByEscolaSede_Id(Long escolaSedeId);
    long countByEscolaSede_Id(Long escolaSedeId);
    boolean existsByAldeia_Id(Long aldeiaId);
    boolean existsByAldeia_IdAndIdNot(Long aldeiaId, Long id);
    long countByAldeia_TerraIndigena_Municipio_Id(Long municipioId);
    long countByAldeia_TerraIndigena_Id(Long terraIndigenaId);
}
