package ac.gov.br.sigei_backend.config.criptografia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;

/**
 * Grava o atributo cifrado e o devolve decifrado à entidade. O Envers usa o mesmo
 * conversor, então as tabelas {@code _aud} também ficam cifradas.
 *
 * <p>O Hibernate cria o conversor pelo contêiner do Spring ao montar o
 * EntityManagerFactory, antes de existir a chave de dados, que é lida do banco.
 * Por isso a {@link CriptografiaDadosSensiveis} é obtida no primeiro uso.
 *
 * <p>Nas fatias {@code @DataJpaTest}, que não carregam os componentes, não há esse
 * bean: o conversor usa a chave configurada como chave de dados, que é o que a
 * primeira inicialização registra. Na aplicação, o bean sempre existe.
 */
@Converter
public class DadoSensivelConverter implements AttributeConverter<String, String> {

    private final ObjectProvider<CriptografiaDadosSensiveis> provedor;
    private final String chave;
    private volatile CriptografiaDadosSensiveis criptografia;

    public DadoSensivelConverter(
            ObjectProvider<CriptografiaDadosSensiveis> provedor,
            @Value("${sigei.dados-sensiveis.chave:}") String chave
    ) {
        this.provedor = provedor;
        this.chave = chave;
    }

    @Override
    public String convertToDatabaseColumn(String valor) {
        return criptografia().cifrar(valor);
    }

    @Override
    public String convertToEntityAttribute(String valor) {
        return criptografia().decifrar(valor);
    }

    private CriptografiaDadosSensiveis criptografia() {
        if (criptografia == null) {
            criptografia = provedor.getIfAvailable(() -> new CriptografiaDadosSensiveis(chave));
        }
        return criptografia;
    }
}
