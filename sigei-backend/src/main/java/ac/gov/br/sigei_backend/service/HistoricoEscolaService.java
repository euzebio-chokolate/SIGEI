package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.CampoAlteradoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

/**
 * Monta a trilha de auditoria de uma escola a partir das revisões do Envers: a
 * própria escola, seus registros anuais e, de cada registro, equipe,
 * infraestrutura e matrículas. Cada versão é comparada com a anterior da mesma
 * entidade para listar apenas os campos alterados.
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

    /** Versões da entidade (incluindo exclusões) como [entidade, Revisao, RevisionType], da mais antiga à mais nova. */
    @SuppressWarnings("unchecked")
    private List<Object[]> versoes(AuditReader auditoria, Class<?> classe, AuditCriterion criterio) {
        return auditoria.createQuery()
                .forRevisionsOfEntity(classe, false, true)
                .add(criterio)
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
    }

    private List<AlteracaoHistoricoDTO> comparar(PersistenceUnitUtil util, List<Object[]> versoes,
                                                 Function<Object, Integer> ano) {
        List<AlteracaoHistoricoDTO> alteracoes = new ArrayList<>();
        Map<Object, Map<String, Object>> versaoAnterior = new HashMap<>();

        for (Object[] versao : versoes) {
            Object entidade = versao[0];
            Revisao revisao = (Revisao) versao[1];
            RevisionType tipo = (RevisionType) versao[2];
            Long id = (Long) util.getIdentifier(entidade);

            Map<String, Object> valores = valores(util, entidade);
            Map<String, Object> anteriores = versaoAnterior.getOrDefault(id, Map.of());

            List<CampoAlteradoDTO> campos = switch (tipo) {
                case ADD -> diferencas(Map.of(), valores);
                case MOD -> diferencas(anteriores, valores);
                case DEL -> diferencas(valores, Map.of());
            };
            versaoAnterior.put(id, tipo == RevisionType.DEL ? Map.of() : valores);

            if (tipo == RevisionType.MOD && campos.isEmpty()) {
                continue;
            }

            alteracoes.add(new AlteracaoHistoricoDTO(
                    revisao.getId(),
                    revisao.getDataHora(),
                    revisao.getUsuarioId(),
                    revisao.getUsuarioNome(),
                    revisao.getUsuarioEmail(),
                    entidade.getClass().getSimpleName(),
                    id,
                    ano.apply(entidade),
                    operacao(tipo),
                    campos
            ));
        }
        return alteracoes;
    }

    /**
     * Campos auditados da versão, na ordem de declaração. Relacionamentos viram
     * {@code <campo>Id}; coleções e o lado inverso de relacionamentos ficam de fora.
     */
    private Map<String, Object> valores(PersistenceUnitUtil util, Object entidade) {
        Map<String, Object> valores = new LinkedHashMap<>();

        for (Field campo : entidade.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(campo.getModifiers())
                    || campo.getName().equals("id")
                    || campo.isAnnotationPresent(NotAudited.class)
                    || campo.isAnnotationPresent(OneToMany.class)
                    || (campo.isAnnotationPresent(OneToOne.class)
                        && !campo.getAnnotation(OneToOne.class).mappedBy().isEmpty())) {
                continue;
            }

            campo.setAccessible(true);
            Object valor;
            try {
                valor = campo.get(entidade);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Campo auditado inacessível: " + campo, e);
            }

            if (campo.getType().isAnnotationPresent(Entity.class)) {
                valores.put(campo.getName() + "Id", idDe(util, valor));
            } else {
                valores.put(campo.getName(), valor);
            }
        }
        return valores;
    }

    private List<CampoAlteradoDTO> diferencas(Map<String, Object> antes, Map<String, Object> depois) {
        Set<String> nomes = new LinkedHashSet<>(antes.keySet());
        nomes.addAll(depois.keySet());

        List<CampoAlteradoDTO> campos = new ArrayList<>();
        for (String nome : nomes) {
            Object anterior = antes.get(nome);
            Object novo = depois.get(nome);
            if (!Objects.equals(anterior, novo)) {
                campos.add(new CampoAlteradoDTO(nome, anterior, novo));
            }
        }
        return campos;
    }

    private static Object idDe(PersistenceUnitUtil util, Object entidade) {
        return entidade == null ? null : util.getIdentifier(entidade);
    }

    private static OperacaoHistorico operacao(RevisionType tipo) {
        return switch (tipo) {
            case ADD -> OperacaoHistorico.CRIACAO;
            case MOD -> OperacaoHistorico.ALTERACAO;
            case DEL -> OperacaoHistorico.EXCLUSAO;
        };
    }
}
