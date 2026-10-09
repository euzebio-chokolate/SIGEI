package ac.gov.br.sigei_backend.service.auditoria;

import ac.gov.br.sigei_backend.config.criptografia.DadoSensivelConverter;
import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.CampoAlteradoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;
import ac.gov.br.sigei_backend.models.Revisao;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditCriterion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Leitura das versões gravadas pelo Envers e comparação de cada versão com a
 * anterior da mesma entidade, para listar só os campos alterados. Usada pela
 * trilha de uma escola ({@code HistoricoEscolaService}) e pela auditoria geral
 * ({@code AuditoriaService}).
 */
public final class VersoesAuditadas {

    /** Valor exibido no lugar de CPF, matrícula e número do contrato. */
    public static final String VALOR_PROTEGIDO = "(dado protegido)";

    /** Campos que identificam o registro para quem lê o histórico, em ordem de preferência. */
    private static final List<String> CAMPOS_DESCRICAO = List.of("nome", "denominacao", "nivel", "ano");

    private static final Map<Class<?>, Set<String>> CAMPOS_SENSIVEIS = new ConcurrentHashMap<>();

    private VersoesAuditadas() {
    }

    /**
     * Uma versão comparada com a anterior. {@code valores} traz todos os campos
     * da versão (úteis para descrever o registro); {@code campos}, só os alterados.
     */
    public record Alteracao(Revisao revisao, Object entidade, Long id, RevisionType tipo,
                            Map<String, Object> valores, List<CampoAlteradoDTO> campos) {

        public String nomeEntidade() {
            return entidade.getClass().getSimpleName();
        }

        public AlteracaoHistoricoDTO paraDTO(Integer ano, String descricao) {
            return new AlteracaoHistoricoDTO(
                    revisao.getId(),
                    revisao.getDataHora(),
                    revisao.getUsuarioId(),
                    revisao.getUsuarioNome(),
                    revisao.getUsuarioEmail(),
                    nomeEntidade(),
                    id,
                    descricao,
                    ano,
                    operacao(tipo),
                    campos
            );
        }
    }

    /** Versões da entidade (incluindo exclusões) como [entidade, Revisao, RevisionType], da mais antiga à mais nova. */
    @SuppressWarnings("unchecked")
    public static List<Object[]> versoes(AuditReader auditoria, Class<?> classe, AuditCriterion... criterios) {
        AuditQuery consulta = auditoria.createQuery().forRevisionsOfEntity(classe, false, true);
        for (AuditCriterion criterio : criterios) {
            consulta.add(criterio);
        }
        return consulta.addOrder(AuditEntity.revisionNumber().asc()).getResultList();
    }

    /**
     * Compara cada versão com a anterior da mesma entidade. Versões de alteração
     * sem diferença nos campos auditados são descartadas. As versões devem vir em
     * ordem crescente de revisão, desde a criação, para a comparação ser correta.
     */
    public static List<Alteracao> comparar(PersistenceUnitUtil util, List<Object[]> versoes) {
        List<Alteracao> alteracoes = new ArrayList<>();
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

            alteracoes.add(new Alteracao(revisao, entidade, id, tipo, valores,
                    protegerSensiveis(entidade.getClass(), campos)));
        }
        return alteracoes;
    }

    /** Nome, série, nível ou ano do próprio registro, quando houver. */
    public static String descricaoPropria(Map<String, Object> valores) {
        for (String campo : CAMPOS_DESCRICAO) {
            Object valor = valores.get(campo);
            if (valor != null && !valor.toString().isBlank()) {
                return valor.toString();
            }
        }
        return null;
    }

    public static Object idDe(PersistenceUnitUtil util, Object entidade) {
        return entidade == null ? null : util.getIdentifier(entidade);
    }

    public static OperacaoHistorico operacao(RevisionType tipo) {
        return switch (tipo) {
            case ADD -> OperacaoHistorico.CRIACAO;
            case MOD -> OperacaoHistorico.ALTERACAO;
            case DEL -> OperacaoHistorico.EXCLUSAO;
        };
    }

    /**
     * Campos auditados da versão, na ordem de declaração. Relacionamentos viram
     * {@code <campo>Id}; coleções e o lado inverso de relacionamentos ficam de fora.
     */
    private static Map<String, Object> valores(PersistenceUnitUtil util, Object entidade) {
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

    private static List<CampoAlteradoDTO> diferencas(Map<String, Object> antes, Map<String, Object> depois) {
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

    /**
     * O Envers lê as tabelas {@code _aud} pelo mesmo conversor da entidade, então
     * CPF, matrícula e número do contrato chegam decifrados. A comparação usa os
     * valores reais (para saber se mudaram), mas a resposta só informa que mudaram.
     */
    private static List<CampoAlteradoDTO> protegerSensiveis(Class<?> classe, List<CampoAlteradoDTO> campos) {
        Set<String> sensiveis = CAMPOS_SENSIVEIS.computeIfAbsent(classe, VersoesAuditadas::camposSensiveis);
        if (sensiveis.isEmpty()) {
            return campos;
        }
        return campos.stream()
                .map(campo -> sensiveis.contains(campo.campo())
                        ? new CampoAlteradoDTO(campo.campo(), protegido(campo.valorAnterior()), protegido(campo.valorNovo()))
                        : campo)
                .toList();
    }

    private static Object protegido(Object valor) {
        return valor == null ? null : VALOR_PROTEGIDO;
    }

    private static Set<String> camposSensiveis(Class<?> classe) {
        Set<String> nomes = new HashSet<>();
        for (Field campo : classe.getDeclaredFields()) {
            Convert conversao = campo.getAnnotation(Convert.class);
            if (conversao != null && conversao.converter() == DadoSensivelConverter.class) {
                nomes.add(campo.getName());
            }
        }
        return Set.copyOf(nomes);
    }
}
