package ac.gov.br.sigei_backend.service.importacao;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Pré-visualização das importações: executa a importação real dentro de uma
 * transação que é sempre desfeita. O resultado (contadores e linhas com erro) é
 * o mesmo que a gravação produziria, porque vem do mesmo código, e nada fica no
 * banco: nem os dados, nem o ano criado pela importação de escolas, nem as
 * revisões de auditoria, que o Envers só grava no commit.
 *
 * <p>As importações de matrículas e de professores, que já são transacionais,
 * participam desta transação; a de escolas, que grava linha a linha fora de
 * transação, passa a gravar dentro dela.
 */
@Component
public class SimulacaoImportacao {

    private final TransactionTemplate transacao;

    public SimulacaoImportacao(PlatformTransactionManager transactionManager) {
        this.transacao = new TransactionTemplate(transactionManager);
    }

    public <T> T simular(Supplier<T> importacao) {
        return transacao.execute(status -> {
            status.setRollbackOnly();
            return importacao.get();
        });
    }
}
