package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.LotacaoProfessor;
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

    List<LotacaoProfessor> findByProfessor_IdOrderByAnoReferencia_AnoDesc(Long professorId);

    List<LotacaoProfessor> findByEscola_Id(Long escolaId);

    boolean existsByAnoReferencia_Ano(Integer ano);
}
