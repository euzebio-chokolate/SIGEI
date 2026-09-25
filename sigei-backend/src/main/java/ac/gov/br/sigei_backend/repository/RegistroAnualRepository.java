package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroAnualRepository extends JpaRepository<RegistroAnual, Long> {
    Optional<RegistroAnual> findById(Long id);
    boolean existsByEscolaAndAnoReferencia(Escola escola, AnoReferencia anoReferencia);
    Optional<RegistroAnual> findTopByEscolaOrderByAnoReferencia_AnoDesc(Escola escola);
    Optional<RegistroAnual> findByEscolaAndAnoReferencia(Escola escola, AnoReferencia anoReferencia);
    Optional<RegistroAnual> findByEscolaAndAnoReferencia_Ano(Escola escola, Integer ano);
    List<RegistroAnual> findByAnoReferencia_Ano(Integer ano);

    @Query("""
            select r from RegistroAnual r
            join fetch r.anoReferencia a
            where r.escola.id = :escolaId
              and (:anoInicial is null or a.ano >= :anoInicial)
              and (:anoFinal is null or a.ano <= :anoFinal)
            order by a.ano asc
            """)
    List<RegistroAnual> buscarHistorico(@Param("escolaId") Long escolaId,
                                      @Param("anoInicial") Integer anoInicial,
                                      @Param("anoFinal") Integer anoFinal);
}
