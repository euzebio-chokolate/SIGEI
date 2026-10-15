package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    boolean existsByEscolaAndAnoReferenciaAndIdNot(Escola escola, AnoReferencia anoReferencia, Long id);
    long countByAnoReferencia_Id(Long anoReferenciaId);
    Optional<RegistroAnual> findTopByEscolaOrderByAnoReferencia_AnoDesc(Escola escola);
    Optional<RegistroAnual> findByEscolaAndAnoReferencia(Escola escola, AnoReferencia anoReferencia);
    Optional<RegistroAnual> findByEscolaAndAnoReferencia_Ano(Escola escola, Integer ano);

    // Equipe e infraestrutura são @OneToOne inversos (EAGER): sem o fetch nas listagens,
    // o Hibernate faz duas consultas por registro, porque esse lado não é carregado em lote
    @EntityGraph(attributePaths = {"anoReferencia", "escola", "equipe", "infraestrutura"})
    List<RegistroAnual> findByAnoReferencia_Ano(Integer ano);

    /** Filtros opcionais; do ano mais recente ao mais antigo e, no ano, pelo nome da escola. */
    @Query(value = """
            select r from RegistroAnual r
            join fetch r.anoReferencia a
            join fetch r.escola e
            left join fetch r.equipe
            left join fetch r.infraestrutura
            left join e.aldeia al
            left join al.terraIndigena t
            where (:ano is null or a.ano = :ano)
              and (:escolaId is null or e.id = :escolaId)
              and (:municipioId is null or t.municipio.id = :municipioId)
            order by a.ano desc, lower(e.nome), r.id
            """,
            countQuery = """
            select count(r) from RegistroAnual r
            join r.anoReferencia a
            join r.escola e
            left join e.aldeia al
            left join al.terraIndigena t
            where (:ano is null or a.ano = :ano)
              and (:escolaId is null or e.id = :escolaId)
              and (:municipioId is null or t.municipio.id = :municipioId)
            """)
    Page<RegistroAnual> buscar(@Param("ano") Integer ano,
                               @Param("escolaId") Long escolaId,
                               @Param("municipioId") Long municipioId,
                               Pageable pageable);

    @Query("""
            select r from RegistroAnual r
            join fetch r.anoReferencia a
            left join fetch r.equipe
            left join fetch r.infraestrutura
            where r.escola.id = :escolaId
              and (:anoInicial is null or a.ano >= :anoInicial)
              and (:anoFinal is null or a.ano <= :anoFinal)
            order by a.ano asc
            """)
    List<RegistroAnual> buscarHistorico(@Param("escolaId") Long escolaId,
                                      @Param("anoInicial") Integer anoInicial,
                                      @Param("anoFinal") Integer anoFinal);
}
