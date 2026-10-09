package ac.gov.br.sigei_backend.service.auditoria;

import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;
import ac.gov.br.sigei_backend.dto.response.historico.PaginaAuditoriaDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.ProfessorRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static ac.gov.br.sigei_backend.service.auditoria.VersoesAuditadas.descricaoPropria;
import static ac.gov.br.sigei_backend.service.auditoria.VersoesAuditadas.idDe;

/**
 * Auditoria geral: as alterações de todas as entidades auditadas, da mais
 * recente à mais antiga, com filtros por entidade, operação, autor e período.
 *
 * <p>A listagem é feita em duas etapas. Primeiro, uma projeção leve (revisão,
 * ID e tipo) de cada tabela {@code _aud} define a ordem e o total. Depois, só
 * os registros da página são carregados e comparados com a versão anterior.
 * Cada alteração recebe uma descrição (a escola ou o professor a que pertence),
 * para fazer sentido fora da tela da escola.
 */
@Service
@Transactional(readOnly = true)
public class AuditoriaService {

    /** As entidades com {@code @Audited}, pelo nome exposto na API. */
    static final Map<String, Class<?>> ENTIDADES = mapaEntidades(
            Escola.class, RegistroAnual.class, EquipeEscola.class, InfraestruturaEscola.class,
            MatriculaNivel.class, MatriculaSerie.class, Aldeia.class, Povo.class, TerraIndigena.class,
            Municipio.class, AnoReferencia.class, Professor.class, LotacaoProfessor.class, Usuario.class);

    public static final int TAMANHO_MAXIMO = 100;

    /** Os períodos são informados em datas do Acre. */
    private static final ZoneId FUSO = ZoneId.of("America/Rio_Branco");

    @PersistenceContext
    EntityManager entityManager;

    @Autowired RegistroAnualRepository registroAnualRepository;
    @Autowired EscolaRepository escolaRepository;
    @Autowired ProfessorRepository professorRepository;
    @Autowired AnoReferenciaRepository anoReferenciaRepository;

    /** Uma alteração na listagem: o registro {@code id} da {@code entidade}, alterado na {@code revisao}. */
    private record Item(long revisao, String entidade, Long id) {}

    public PaginaAuditoriaDTO buscar(FiltroAuditoria filtro, int pagina, int tamanho) {
        validar(filtro, pagina, tamanho);
        AuditReader auditoria = AuditReaderFactory.get(entityManager);

        List<Item> itens = new ArrayList<>();
        for (Map.Entry<String, Class<?>> entidade : ENTIDADES.entrySet()) {
            if (filtro.entidade() == null || filtro.entidade().equals(entidade.getKey())) {
                itens.addAll(listarItens(auditoria, entidade.getKey(), entidade.getValue(), filtro));
            }
        }
        itens.sort(Comparator.comparingLong(Item::revisao).reversed()
                .thenComparing(Item::entidade)
                .thenComparing(Item::id));

        int inicio = (int) Math.min((long) pagina * tamanho, itens.size());
        List<Item> itensDaPagina = itens.subList(inicio, Math.min(inicio + tamanho, itens.size()));
        int totalPaginas = (int) Math.ceil((double) itens.size() / tamanho);

        return new PaginaAuditoriaDTO(carregar(auditoria, itensDaPagina), pagina, tamanho, itens.size(), totalPaginas);
    }

    private void validar(FiltroAuditoria filtro, int pagina, int tamanho) {
        if (filtro.entidade() != null && !ENTIDADES.containsKey(filtro.entidade())) {
            throw new IllegalArgumentException("Tipo de registro inválido: use um de " + String.join(", ", ENTIDADES.keySet()) + ".");
        }
        if (pagina < 0) {
            throw new IllegalArgumentException("A página deve ser zero ou maior.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        if (filtro.de() != null && filtro.ate() != null && filtro.de().isAfter(filtro.ate())) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
        if (filtro.usuarioId() != null && Boolean.TRUE.equals(filtro.sistema())) {
            throw new IllegalArgumentException("Filtre por um usuário ou pelas alterações do sistema, não pelos dois.");
        }
    }

    /** Projeção leve (revisão, ID, tipo) das versões da entidade que atendem aos filtros. */
    @SuppressWarnings("unchecked")
    private List<Item> listarItens(AuditReader auditoria, String nome, Class<?> classe, FiltroAuditoria filtro) {
        AuditQuery consulta = auditoria.createQuery()
                .forRevisionsOfEntity(classe, false, true)
                .addProjection(AuditEntity.revisionNumber())
                .addProjection(AuditEntity.id());

        if (filtro.operacao() != null) {
            consulta.add(AuditEntity.revisionType().eq(tipoEnvers(filtro.operacao())));
        }
        if (filtro.usuarioId() != null) {
            consulta.add(AuditEntity.revisionProperty("usuarioId").eq(filtro.usuarioId()));
        }
        if (Boolean.TRUE.equals(filtro.sistema())) {
            consulta.add(AuditEntity.revisionProperty("usuarioId").isNull());
        }
        if (filtro.de() != null) {
            consulta.add(AuditEntity.revisionProperty("dataHora").ge(filtro.de().atStartOfDay(FUSO).toInstant()));
        }
        if (filtro.ate() != null) {
            consulta.add(AuditEntity.revisionProperty("dataHora").lt(filtro.ate().plusDays(1).atStartOfDay(FUSO).toInstant()));
        }

        List<Object[]> linhas = consulta.getResultList();
        return linhas.stream()
                .map(linha -> new Item(((Number) linha[0]).longValue(), nome, ((Number) linha[1]).longValue()))
                .toList();
    }

    /** Carrega e compara só os registros da página, preservando a ordem da listagem. */
    private List<AlteracaoHistoricoDTO> carregar(AuditReader auditoria, List<Item> itensDaPagina) {
        if (itensDaPagina.isEmpty()) {
            return List.of();
        }
        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Contexto contexto = new Contexto(auditoria, util);

        Map<Item, AlteracaoHistoricoDTO> porItem = new HashMap<>();
        Map<String, List<Item>> porEntidade = itensDaPagina.stream().collect(Collectors.groupingBy(Item::entidade));

        for (Map.Entry<String, List<Item>> grupo : porEntidade.entrySet()) {
            Object[] ids = grupo.getValue().stream().map(Item::id).distinct().toArray();
            long ultimaRevisao = grupo.getValue().stream().mapToLong(Item::revisao).max().orElseThrow();

            // Todas as versões até a última da página, para comparar cada uma com a anterior
            List<Object[]> versoes = VersoesAuditadas.versoes(auditoria, ENTIDADES.get(grupo.getKey()),
                    AuditEntity.id().in(ids), AuditEntity.revisionNumber().le(ultimaRevisao));

            for (VersoesAuditadas.Alteracao alteracao : VersoesAuditadas.comparar(util, versoes)) {
                Item item = new Item(alteracao.revisao().getId(), grupo.getKey(), alteracao.id());
                if (grupo.getValue().contains(item)) {
                    porItem.put(item, contexto.descrever(alteracao));
                }
            }
        }

        // Versões de alteração sem diferença nos campos auditados não aparecem
        return itensDaPagina.stream().map(porItem::get).filter(Objects::nonNull).toList();
    }

    private static RevisionType tipoEnvers(OperacaoHistorico operacao) {
        return switch (operacao) {
            case CRIACAO -> RevisionType.ADD;
            case ALTERACAO -> RevisionType.MOD;
            case EXCLUSAO -> RevisionType.DEL;
        };
    }

    private static Map<String, Class<?>> mapaEntidades(Class<?>... classes) {
        Map<String, Class<?>> mapa = new LinkedHashMap<>();
        for (Class<?> classe : classes) {
            mapa.put(classe.getSimpleName(), classe);
        }
        return Collections.unmodifiableMap(mapa);
    }

    /**
     * Descreve cada alteração pelo registro a que pertence. Busca nos dados
     * atuais e, se o registro já foi excluído, na última versão auditada.
     * Guarda o que já resolveu, pois os itens de uma página costumam se repetir.
     */
    private final class Contexto {
        private final AuditReader auditoria;
        private final PersistenceUnitUtil util;
        private final Map<Long, String> nomeEscola = new HashMap<>();
        private final Map<Long, String> nomeProfessor = new HashMap<>();
        private final Map<Long, Integer> anoPorReferencia = new HashMap<>();
        /** Registro anual → [escolaId, anoReferenciaId]. */
        private final Map<Long, Long[]> registros = new HashMap<>();

        Contexto(AuditReader auditoria, PersistenceUnitUtil util) {
            this.auditoria = auditoria;
            this.util = util;
        }

        AlteracaoHistoricoDTO descrever(VersoesAuditadas.Alteracao alteracao) {
            Map<String, Object> valores = alteracao.valores();
            String propria = descricaoPropria(valores);

            return switch (alteracao.nomeEntidade()) {
                case "RegistroAnual" -> doRegistro(alteracao, alteracao.id(), null);
                case "EquipeEscola", "InfraestruturaEscola" ->
                        doRegistro(alteracao, comoLong(valores.get("registroAnualId")), null);
                case "MatriculaNivel", "MatriculaSerie" ->
                        doRegistro(alteracao, comoLong(valores.get("registroId")), propria);
                case "LotacaoProfessor" -> {
                    Long professorId = comoLong(valores.get("professorId"));
                    String professor = professorId == null ? "Vaga em aberto" : nomeProfessor(professorId);
                    yield alteracao.paraDTO(ano(comoLong(valores.get("anoReferenciaId"))), professor);
                }
                default -> alteracao.paraDTO(null, propria);
            };
        }

        /** Escola e ano do registro anual; {@code detalhe} (nível ou série) vai depois do nome da escola. */
        private AlteracaoHistoricoDTO doRegistro(VersoesAuditadas.Alteracao alteracao, Long registroId, String detalhe) {
            Long[] registro = registroId == null ? null : registros.computeIfAbsent(registroId, this::buscarRegistro);
            if (registro == null) {
                return alteracao.paraDTO(null, detalhe);
            }
            String escola = registro[0] == null ? null : nomeEscola(registro[0]);
            String descricao = detalhe == null ? escola : (escola == null ? detalhe : escola + " · " + detalhe);
            return alteracao.paraDTO(ano(registro[1]), descricao);
        }

        private Long[] buscarRegistro(Long id) {
            Optional<RegistroAnual> atual = registroAnualRepository.findById(id);
            if (atual.isPresent()) {
                return new Long[]{comoLong(idDe(util, atual.get().getEscola())), comoLong(idDe(util, atual.get().getAnoReferencia()))};
            }
            RegistroAnual excluido = ultimaVersao(RegistroAnual.class, id);
            return excluido == null ? null
                    : new Long[]{comoLong(idDe(util, excluido.getEscola())), comoLong(idDe(util, excluido.getAnoReferencia()))};
        }

        private String nomeEscola(Long id) {
            return nomeEscola.computeIfAbsent(id, chave -> escolaRepository.findById(chave)
                    .map(Escola::getNome)
                    .orElseGet(() -> Optional.ofNullable(ultimaVersao(Escola.class, chave)).map(Escola::getNome).orElse(null)));
        }

        private String nomeProfessor(Long id) {
            return nomeProfessor.computeIfAbsent(id, chave -> professorRepository.findById(chave)
                    .map(Professor::getNome)
                    .orElseGet(() -> Optional.ofNullable(ultimaVersao(Professor.class, chave)).map(Professor::getNome).orElse(null)));
        }

        private Integer ano(Long anoReferenciaId) {
            if (anoReferenciaId == null) return null;
            return anoPorReferencia.computeIfAbsent(anoReferenciaId, chave -> anoReferenciaRepository.findById(chave)
                    .map(AnoReferencia::getAno).orElse(null));
        }

        /** Última versão gravada (para excluídos, os valores no momento da exclusão). */
        @SuppressWarnings("unchecked")
        private <T> T ultimaVersao(Class<T> classe, Long id) {
            List<T> versoes = auditoria.createQuery()
                    .forRevisionsOfEntity(classe, true, true)
                    .add(AuditEntity.id().eq(id))
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .setMaxResults(1)
                    .getResultList();
            return versoes.isEmpty() ? null : versoes.getFirst();
        }

        private static Long comoLong(Object valor) {
            return valor instanceof Number numero ? numero.longValue() : null;
        }
    }
}
