package ac.gov.br.sigei_backend.config.criptografia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

/**
 * Grava o atributo cifrado e o devolve decifrado à entidade. O Envers usa o mesmo
 * conversor, então as tabelas {@code _aud} também ficam cifradas.
 *
 * <p>O Hibernate cria o conversor pelo contêiner do Spring, que resolve a chave pelo
 * {@code @Value}. A chave é lida diretamente, e não por um bean, para que o conversor
 * funcione também nos testes {@code @DataJpaTest}, que não carregam os componentes.
 */
@Converter
public class DadoSensivelConverter implements AttributeConverter<String, String> {

    private final CriptografiaDadosSensiveis criptografia;

    public DadoSensivelConverter(
            @Value("${sigei.dados-sensiveis.chave:}") String chave
    ) {
        this.criptografia = new CriptografiaDadosSensiveis(chave);
    }

    @Override
    public String convertToDatabaseColumn(String valor) {
        return criptografia.cifrar(valor);
    }

    @Override
    public String convertToEntityAttribute(String valor) {
        return criptografia.decifrar(valor);
    }
}
