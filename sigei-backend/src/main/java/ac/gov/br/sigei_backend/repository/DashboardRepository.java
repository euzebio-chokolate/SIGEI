package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardEvolucaoDTO;
import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardItemDTO;
import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardResumoDTO;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.projection.DashboardInfraestruturaProjection;
import ac.gov.br.sigei_backend.repository.projection.DashboardProfessoresProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardRepository extends Repository<RegistroAnual, Long> {

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardResumoDTO(
                count(distinct e.id),
                coalesce(sum(r.totalAlunos), 0),
                count(distinct m.id),
                count(distinct t.id)
            )
            from RegistroAnual r
            left join r.escola e
            left join e.aldeia a
            left join a.terraIndigena t
            left join t.municipio m
            where r.anoReferencia.ano = :ano
            """)
    DashboardResumoDTO buscarResumo(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardEvolucaoDTO(
                r.anoReferencia.ano,
                coalesce(sum(r.totalAlunos), 0)
            )
            from RegistroAnual r
            group by r.anoReferencia.ano
            order by r.anoReferencia.ano
            """)
    List<DashboardEvolucaoDTO> buscarEvolucaoMatriculas();

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardEvolucaoDTO(
                r.anoReferencia.ano,
                count(distinct r.escola.id)
            )
            from RegistroAnual r
            group by r.anoReferencia.ano
            order by r.anoReferencia.ano
            """)
    List<DashboardEvolucaoDTO> buscarEvolucaoEscolas();

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardItemDTO(
                m.nivel,
                coalesce(sum(m.total), 0)
            )
            from MatriculaNivel m
            where m.registro.anoReferencia.ano = :ano
            group by m.nivel
            order by sum(m.total) desc
            """)
    List<DashboardItemDTO> buscarMatriculasPorNivel(@Param("ano") Integer ano);

    @Query("""
            select
                count(i) as totalAvaliado,
                coalesce(sum(case when i.temInternet = true then 1 else 0 end), 0) as comInternet,
                coalesce(sum(case
                    when i.tipoEnergia is not null
                         and trim(i.tipoEnergia) <> ''
                         and lower(trim(i.tipoEnergia)) not in ('não', 'nao', 'nenhuma', 'sem energia')
                    then 1 else 0 end), 0) as comEnergia,
                coalesce(sum(case when i.temPredio = true then 1 else 0 end), 0) as comPredio
            from InfraestruturaEscola i
            where i.registroAnual.anoReferencia.ano = :ano
            """)
    DashboardInfraestruturaProjection buscarInfraestrutura(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardItemDTO(
                r.escola.aldeia.terraIndigena.municipio.nome,
                count(distinct r.escola.id)
            )
            from RegistroAnual r
            where r.anoReferencia.ano = :ano
              and r.escola.aldeia.terraIndigena.municipio is not null
            group by r.escola.aldeia.terraIndigena.municipio.id,
                     r.escola.aldeia.terraIndigena.municipio.nome
            order by count(distinct r.escola.id) desc
            """)
    List<DashboardItemDTO> buscarEscolasPorMunicipio(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardItemDTO(
                i.situacao,
                count(i)
            )
            from InfraestruturaEscola i
            where i.registroAnual.anoReferencia.ano = :ano
            group by i.situacao
            order by count(i) desc
            """)
    List<DashboardItemDTO> buscarSituacaoEscolas(@Param("ano") Integer ano);

    @Query("""
            select
                count(distinct p.id) as totalProfessores,
                count(l) as totalLotacoes,
                coalesce(sum(case when p.id is null then 1 else 0 end), 0) as vagasEmAberto,
                coalesce(sum(case when e.id is null then 1 else 0 end), 0) as lotacoesNoNucleo
            from LotacaoProfessor l
            left join l.professor p
            left join l.escola e
            where l.anoReferencia.ano = :ano
            """)
    DashboardProfessoresProjection buscarProfessores(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.dto.response.dashboard.DashboardItemDTO(
                l.tipoContrato,
                count(l)
            )
            from LotacaoProfessor l
            where l.anoReferencia.ano = :ano
            group by l.tipoContrato
            order by count(l) desc
            """)
    List<DashboardItemDTO> buscarLotacoesPorTipoContrato(@Param("ano") Integer ano);
}
