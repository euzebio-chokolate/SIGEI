package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.projection.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Consultas dos relatórios exportáveis. Selecionam projeções, sem materializar
 * entidades; o filtro por município é aplicado pelo serviço.
 */
public interface RelatorioRepository extends Repository<RegistroAnual, Long> {

    @Query("""
            select new ac.gov.br.sigei_backend.repository.projection.LinhaEscolaRelatorio(
                r.id, m.id, m.nome, t.nome, a.nome, p.nome, e.codigo, e.nome, e.tipoUnidade, s.nome,
                r.totalAlunos, i.situacao, i.tipoAcesso, i.temPredio, i.numSalas, i.temInternet,
                i.tipoAgua, i.tipoEnergia, i.id
            )
            from RegistroAnual r
            join r.escola e
            left join e.escolaSede s
            left join e.aldeia a
            left join a.povo p
            left join a.terraIndigena t
            left join t.municipio m
            left join r.infraestrutura i
            where r.anoReferencia.ano = :ano
            order by m.nome, e.nome
            """)
    List<LinhaEscolaRelatorio> buscarEscolas(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.repository.projection.MatriculaNivelRelatorio(
                n.registro.id, n.nivel, n.total, n.qtdMasculino, n.qtdFeminino
            )
            from MatriculaNivel n
            where n.registro.anoReferencia.ano = :ano
            """)
    List<MatriculaNivelRelatorio> buscarMatriculasPorNivel(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.repository.projection.MatriculaSerieRelatorio(
                s.registro.id, s.denominacao, s.etapaEnsino, s.total
            )
            from MatriculaSerie s
            where s.registro.anoReferencia.ano = :ano
            """)
    List<MatriculaSerieRelatorio> buscarMatriculasPorSerie(@Param("ano") Integer ano);

    @Query("""
            select new ac.gov.br.sigei_backend.repository.projection.EvolucaoRelatorio(
                r.anoReferencia.ano, m.id, count(distinct r.escola.id), coalesce(sum(r.totalAlunos), 0)
            )
            from RegistroAnual r
            left join r.escola.aldeia a
            left join a.terraIndigena t
            left join t.municipio m
            group by r.anoReferencia.ano, m.id
            order by r.anoReferencia.ano
            """)
    List<EvolucaoRelatorio> buscarEvolucao();

    @Query("""
            select new ac.gov.br.sigei_backend.repository.projection.LotacaoRelatorio(
                m.id, m.nome, e.nome, p.id, p.nome, p.etnia, p.sexo, p.formacao,
                l.cargo, l.segmento, l.tipoContrato, l.inicioContrato
            )
            from LotacaoProfessor l
            join l.municipio m
            left join l.escola e
            left join l.professor p
            where l.anoReferencia.ano = :ano
            order by m.nome, e.nome, p.nome
            """)
    List<LotacaoRelatorio> buscarLotacoes(@Param("ano") Integer ano);
}
