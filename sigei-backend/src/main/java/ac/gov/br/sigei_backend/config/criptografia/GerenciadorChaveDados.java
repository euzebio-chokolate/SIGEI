package ac.gov.br.sigei_backend.config.criptografia;

import ac.gov.br.sigei_backend.models.ChaveDados;
import ac.gov.br.sigei_backend.repository.ChaveDadosRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolve, na inicialização, a chave de dados que cifra CPF, matrícula e número
 * do contrato (cifra em envelope). A chave de dados fica no banco
 * ({@link ChaveDados}), cifrada pela chave mestra e, se configurada, pela chave
 * pública de recuperação; a chave mestra nunca cifra os dados diretamente.
 *
 * <ul>
 *   <li><b>Primeira inicialização:</b> a chave de dados é a chave mestra atual, que
 *       já cifra os dados existentes; nada precisa ser recifrado.</li>
 *   <li><b>Troca da chave mestra:</b> com a nova em {@code SIGEI_CHAVE_DADOS_SENSIVEIS}
 *       e a antiga em {@code SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR}, o registro é
 *       recifrado com a nova; depois, a anterior pode ser removida.</li>
 *   <li><b>Perda da chave mestra:</b> com uma nova chave mestra e a chave privada de
 *       recuperação em {@code SIGEI_CHAVE_RECUPERACAO_PRIVADA}, o registro é
 *       recifrado com a nova; depois, a chave privada deve ser removida do ambiente.</li>
 * </ul>
 *
 * <p>Se nenhuma chave abrir o registro, ou se a chave de dados não decifrar os dados
 * gravados, a aplicação não sobe: gravar com a chave errada misturaria valores
 * que nenhuma chave lê.
 */
@Component
public class GerenciadorChaveDados {

    static final String VARIAVEL_MESTRA = "SIGEI_CHAVE_DADOS_SENSIVEIS";
    static final String VARIAVEL_ANTERIOR = "SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR";
    static final String VARIAVEL_PUBLICA = "SIGEI_CHAVE_RECUPERACAO_PUBLICA";
    static final String VARIAVEL_PRIVADA = "SIGEI_CHAVE_RECUPERACAO_PRIVADA";

    private static final Logger log = LoggerFactory.getLogger(GerenciadorChaveDados.class);

    /** Colunas cifradas usadas para conferir a chave na inicialização. */
    private static final List<String> AMOSTRAS = List.of(
            "select cpf from professor where cpf is not null",
            "select matricula from professor where matricula is not null",
            "select numero_contrato from lotacao_professor where numero_contrato is not null"
    );

    private final ChaveDadosRepository repository;
    private final TransactionTemplate transacao;
    private final String chaveMestra;
    private final String chaveAnterior;
    private final String chavePublica;
    private final String chavePrivada;
    private final EntityManager entityManager;

    private byte[] chaveDados;

    public GerenciadorChaveDados(
            ChaveDadosRepository repository,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager,
            @Value("${sigei.dados-sensiveis.chave:}") String chaveMestra,
            @Value("${sigei.dados-sensiveis.chave-anterior:}") String chaveAnterior,
            @Value("${sigei.dados-sensiveis.recuperacao.chave-publica:}") String chavePublica,
            @Value("${sigei.dados-sensiveis.recuperacao.chave-privada:}") String chavePrivada
    ) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.transacao = new TransactionTemplate(transactionManager);
        this.chaveMestra = chaveMestra;
        this.chaveAnterior = chaveAnterior;
        this.chavePublica = chavePublica;
        this.chavePrivada = chavePrivada;
    }

    /** A chave de dados, resolvida (e registrada, se preciso) na primeira chamada. */
    public synchronized byte[] chaveDados() {
        if (chaveDados == null) {
            chaveDados = transacao.execute(status -> resolver());
        }
        return chaveDados.clone();
    }

    private byte[] resolver() {
        byte[] mestra = EnvelopeChave.decodificarChaveMestra(chaveMestra, VARIAVEL_MESTRA);
        Optional<PublicKey> publica = configurada(chavePublica)
                .map(texto -> EnvelopeChave.lerChavePublica(texto, VARIAVEL_PUBLICA));

        ChaveDados registro = repository.findById(ChaveDados.ID).orElse(null);
        String mestraAntes = registro == null ? null : registro.getCifradaMestra();
        String recuperacaoAntes = registro == null ? null : registro.getCifradaRecuperacao();
        byte[] dados;

        if (registro == null) {
            dados = mestra;
            registro = new ChaveDados();
            registro.setId(ChaveDados.ID);
            registro.setCifradaMestra(EnvelopeChave.cifrarComMestra(mestra, dados));
            log.info("Chave de dados registrada a partir da chave mestra atual; os dados existentes não mudam.");
        } else {
            dados = abrir(registro, mestra);
        }

        conferirDadosGravados(new CriptografiaDadosSensiveis(dados));

        if (publica.isPresent()) {
            String impressao = EnvelopeChave.impressao(publica.get());
            if (!impressao.equals(registro.getImpressaoRecuperacao())) {
                registro.setCifradaRecuperacao(EnvelopeChave.cifrarParaRecuperacao(publica.get(), dados));
                registro.setImpressaoRecuperacao(impressao);
                log.info("Cópia de recuperação da chave de dados atualizada (chave pública {}).",
                        impressao.substring(0, 16));
            }
        } else if (registro.getCifradaRecuperacao() == null) {
            log.warn("Sem {}: se {} for perdida, CPF, matrícula e número do contrato ficarão irrecuperáveis.",
                    VARIAVEL_PUBLICA, VARIAVEL_MESTRA);
        }

        if (!registro.getCifradaMestra().equals(mestraAntes)
                || !Objects.equals(registro.getCifradaRecuperacao(), recuperacaoAntes)) {
            Instant agora = Instant.now();
            if (registro.getCriadaEm() == null) {
                registro.setCriadaEm(agora);
            }
            registro.setAtualizadaEm(agora);
            repository.save(registro);
        }
        return dados;
    }

    /** Abre o registro com a chave mestra; se não der, tenta a anterior e a recuperação. */
    private byte[] abrir(ChaveDados registro, byte[] mestra) {
        Optional<byte[]> pelaMestra = EnvelopeChave.decifrarComMestra(mestra, registro.getCifradaMestra());
        if (pelaMestra.isPresent()) {
            if (configurada(chaveAnterior).isPresent()) {
                log.warn("{} já pode ser removida: a chave de dados está cifrada com a chave mestra atual.",
                        VARIAVEL_ANTERIOR);
            }
            if (configurada(chavePrivada).isPresent()) {
                log.warn("Remova {} do ambiente: a chave mestra atual já abre a chave de dados.", VARIAVEL_PRIVADA);
            }
            return pelaMestra.get();
        }

        Optional<byte[]> pelaAnterior = configurada(chaveAnterior)
                .map(texto -> EnvelopeChave.decodificarChaveMestra(texto, VARIAVEL_ANTERIOR))
                .flatMap(anterior -> EnvelopeChave.decifrarComMestra(anterior, registro.getCifradaMestra()));
        if (pelaAnterior.isPresent()) {
            registro.setCifradaMestra(EnvelopeChave.cifrarComMestra(mestra, pelaAnterior.get()));
            log.warn("Chave mestra trocada: a chave de dados foi recifrada com a nova. Remova {}.", VARIAVEL_ANTERIOR);
            return pelaAnterior.get();
        }

        Optional<byte[]> pelaRecuperacao = configurada(chavePrivada)
                .map(texto -> EnvelopeChave.lerChavePrivada(texto, VARIAVEL_PRIVADA))
                .flatMap(privada -> EnvelopeChave.decifrarComRecuperacao(privada, registro.getCifradaRecuperacao()));
        if (pelaRecuperacao.isPresent()) {
            registro.setCifradaMestra(EnvelopeChave.cifrarComMestra(mestra, pelaRecuperacao.get()));
            log.warn("Chave de dados recuperada pela chave de recuperação e recifrada com a nova chave mestra. " +
                    "Remova {} do ambiente agora.", VARIAVEL_PRIVADA);
            return pelaRecuperacao.get();
        }

        throw new IllegalStateException(VARIAVEL_MESTRA + " não abre a chave de dados gravada no banco. " +
                "Se a chave mestra foi trocada, informe a antiga em " + VARIAVEL_ANTERIOR +
                "; se foi perdida, informe a chave privada de recuperação em " + VARIAVEL_PRIVADA + ".");
    }

    /**
     * Confere uma amostra de cada coluna cifrada: uma chave de dados que não as
     * decifra (banco restaurado de outro ambiente, chave mestra errada na primeira
     * inicialização) impede a aplicação de subir.
     */
    private void conferirDadosGravados(CriptografiaDadosSensiveis criptografia) {
        for (String consulta : AMOSTRAS) {
            List<?> valores = entityManager.createNativeQuery(consulta).setMaxResults(1).getResultList();

            for (Object valor : valores) {
                try {
                    criptografia.decifrar((String) valor);
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("A chave de dados não decifra os dados sensíveis já gravados. " +
                            "Confira " + VARIAVEL_MESTRA + ": ela deve ser a mesma usada para gravá-los.", e);
                }
            }
        }
    }

    private static Optional<String> configurada(String valor) {
        return valor == null || valor.isBlank() ? Optional.empty() : Optional.of(valor);
    }
}
