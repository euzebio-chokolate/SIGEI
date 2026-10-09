package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.service.auditoria.VersoesAuditadas;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

import static ac.gov.br.sigei_backend.service.auditoria.VersoesAuditadas.idDe;
import static ac.gov.br.sigei_backend.service.auditoria.VersoesAuditadas.versoes;

/**
 * Monta a trilha de auditoria de uma escola a partir das revisões do Envers: a
 * própria escola, seus registros anuais e, de cada registro, equipe,
 * infraestrutura e matrículas. Cada versão é comparada com a anterior da mesma
 * entidade para listar apenas os campos alterados ({@link VersoesAuditadas}).
 */
@Service
@Transactional(readOnly = true)
public class HistoricoEscolaService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    public List<AlteracaoHistoricoDTO> buscarPorEscola(Long escolaId) {
        AuditReader auditoria = AuditReaderFactory.get(entityManager);
        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        List<Object[]> versoesEscola = versoes(auditoria, Escola.class, AuditEntity.id().eq(escolaId));
        if (versoesEscola.isEmpty()) {
            throw new NoSuchElementException("Escola não encontrada");
        }

        List<Object[]> versoesRegistro = versoes(auditoria, RegistroAnual.class,
                AuditEntity.relatedId("escola").eq(escolaId));

        Map<Object, Integer> anoPorRegistro = new HashMap<>();
        Map<Object, Integer> anoPorReferencia = new HashMap<>();
        for (Object[] versao : versoesRegistro) {
            RegistroAnual registro = (RegistroAnual) versao[0];
            Object anoReferenciaId = idDe(util, registro.getAnoReferencia());
            if (anoReferenciaId != null) {
                anoPorRegistro.put(registro.getId(), anoPorReferencia.computeIfAbsent(anoReferenciaId,
                        id -> anoReferenciaRepository.findById((Long) id).map(AnoReferencia::getAno).orElse(null)));
            }
        }

        List<AlteracaoHistoricoDTO> alteracoes = new ArrayList<>();
        alteracoes.addAll(comparar(util, versoesEscola, escola -> null));
        alteracoes.addAll(comparar(util, versoesRegistro,
                registro -> anoPorRegistro.get(((RegistroAnual) registro).getId())));

        Set<Object> registroIds = new HashSet<>();
        versoesRegistro.forEach(versao -> registroIds.add(((RegistroAnual) versao[0]).getId()));

        if (!registroIds.isEmpty()) {
            Object[] ids = registroIds.toArray();
            alteracoes.addAll(comparar(util,
                    versoes(auditoria, EquipeEscola.class, AuditEntity.relatedId("registroAnual").in(ids)),
                    equipe -> anoPorRegistro.get(idDe(util, ((EquipeEscola) equipe).getRegistroAnual()))));
            alteracoes.addAll(comparar(util,
                    versoes(auditoria, InfraestruturaEscola.class, AuditEntity.relatedId("registroAnual").in(ids)),
                    infra -> anoPorRegistro.get(idDe(util, ((InfraestruturaEscola) infra).getRegistroAnual()))));
            alteracoes.addAll(comparar(util,
                    versoes(auditoria, MatriculaNivel.class, AuditEntity.relatedId("registro").in(ids)),
                    nivel -> anoPorRegistro.get(idDe(util, ((MatriculaNivel) nivel).getRegistro()))));
            alteracoes.addAll(comparar(util,
                    versoes(auditoria, MatriculaSerie.class, AuditEntity.relatedId("registro").in(ids)),
                    serie -> anoPorRegistro.get(idDe(util, ((MatriculaSerie) serie).getRegistro()))));
        }

        alteracoes.sort(Comparator.comparing(AlteracaoHistoricoDTO::revisao).reversed()
                .thenComparing(AlteracaoHistoricoDTO::entidade)
                .thenComparing(AlteracaoHistoricoDTO::entidadeId));
        return alteracoes;
    }

    private List<AlteracaoHistoricoDTO> comparar(PersistenceUnitUtil util, List<Object[]> versoes,
                                                 Function<Object, Integer> ano) {
        return VersoesAuditadas.comparar(util, versoes).stream()
                .map(alteracao -> alteracao.paraDTO(
                        ano.apply(alteracao.entidade()),
                        VersoesAuditadas.descricaoPropria(alteracao.valores())))
                .toList();
    }
}
