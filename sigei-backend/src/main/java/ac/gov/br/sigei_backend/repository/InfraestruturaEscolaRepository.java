package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfraestruturaEscolaRepository extends JpaRepository<InfraestruturaEscola, Long> {
    Optional<InfraestruturaEscola> findById(Long id);
    Optional<InfraestruturaEscola> findTopByRegistroAnualEscolaOrderByRegistroAnualAnoReferenciaAnoDesc(Escola escola);
    Optional<InfraestruturaEscola> findByRegistroAnual_Id(Long registroAnualId);
    boolean existsByRegistroAnual_Id(Long registroAnualId);
    boolean existsByRegistroAnual_IdAndIdNot(Long registroAnualId, Long id);
    Optional<InfraestruturaEscola> findTopByRegistroAnual_EscolaOrderByRegistroAnual_AnoReferencia_AnoDesc(Escola escola);
    List<InfraestruturaEscola> findByRegistroAnual_AnoReferencia_Ano(Integer ano);

    /** Uma infraestrutura por escola, sem materializar o grafo EAGER de registros anuais. */
    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO(
                i.id, e.id, e.codigo, e.nome, a.id, a.ano,
                i.anoConstrucao, i.numSalas, i.latitude, i.longitude,
                i.situacao, i.tipoAcesso, i.temPredio, i.tipoAgua,
                i.temInternet, i.tipoEnergia, i.temRegistroFotografico, i.fotoUrl
            )
            from InfraestruturaEscola i
            join i.registroAnual r
            join r.escola e
            join r.anoReferencia a
            where a.ano = (
                select max(r2.anoReferencia.ano) from RegistroAnual r2
                where r2.escola = r.escola and r2.infraestrutura is not null
            )
            """)
    List<InfraestruturaEscolaResponseDTO> buscarMaisRecentesPorEscola();
}
