package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.LotacaoProfessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotacaoProfessorRepository extends JpaRepository<LotacaoProfessor, Long> {

    @Query("""
            select l from LotacaoProfessor l
            join fetch l.anoReferencia a
            join fetch l.municipio
            left join fetch l.professor
            left join fetch l.escola
            where a.ano = :ano
            """)
    List<LotacaoProfessor> buscarPorAno(@Param("ano") Integer ano);

    /**
     * Filtros opcionais; do ano mais recente ao mais antigo e, no ano, na ordem da
     * tela: município, escola (núcleo primeiro) e professor (vagas por último).
     */
    @Query(value = """
            select l from LotacaoProfessor l
            join fetch l.anoReferencia a
            join fetch l.municipio m
            left join fetch l.professor p
            left join fetch l.escola e
            where (:ano is null or a.ano = :ano)
              and (:professorId is null or p.id = :professorId)
              and (:escolaId is null or e.id = :escolaId)
              and (:municipioId is null or m.id = :municipioId)
            order by a.ano desc, lower(m.nome), lower(e.nome) nulls first, lower(p.nome) nulls last, l.id
            """,
            countQuery = """
            select count(l) from LotacaoProfessor l
            join l.anoReferencia a
            left join l.professor p
            left join l.escola e
            where (:ano is null or a.ano = :ano)
              and (:professorId is null or p.id = :professorId)
              and (:escolaId is null or e.id = :escolaId)
              and (:municipioId is null or l.municipio.id = :municipioId)
            """)
    Page<LotacaoProfessor> buscar(@Param("ano") Integer ano,
                                  @Param("professorId") Long professorId,
                                  @Param("escolaId") Long escolaId,
                                  @Param("municipioId") Long municipioId,
                                  Pageable pageable);

    List<LotacaoProfessor> findByProfessor_IdOrderByAnoReferencia_AnoDesc(Long professorId);

    List<LotacaoProfessor> findByEscola_Id(Long escolaId);

    boolean existsByAnoReferencia_Ano(Integer ano);

    long countByAnoReferencia_Id(Long anoReferenciaId);

    long countByMunicipio_Id(Long municipioId);
}
