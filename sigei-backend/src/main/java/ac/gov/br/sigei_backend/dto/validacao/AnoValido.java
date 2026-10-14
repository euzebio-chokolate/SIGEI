package ac.gov.br.sigei_backend.dto.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ano entre {@link #min()} e o ano atual do Acre somado a {@link #anosAFrente()}.
 * O limite superior muda com o tempo, por isso não cabe em {@code @Max}. Nulo é
 * válido; use {@code @NotNull} quando o campo for obrigatório.
 */
@Documented
@Constraint(validatedBy = AnoValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnoValido {

    String message();

    int min() default 1900;

    int anosAFrente() default 0;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
