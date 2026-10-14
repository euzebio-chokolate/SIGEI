package ac.gov.br.sigei_backend.dto.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;
import java.time.ZoneId;

public class AnoValidoValidator implements ConstraintValidator<AnoValido, Integer> {

    private static final ZoneId FUSO = ZoneId.of("America/Rio_Branco");

    private int min;
    private int anosAFrente;

    @Override
    public void initialize(AnoValido anotacao) {
        min = anotacao.min();
        anosAFrente = anotacao.anosAFrente();
    }

    @Override
    public boolean isValid(Integer ano, ConstraintValidatorContext contexto) {
        return ano == null || (ano >= min && ano <= Year.now(FUSO).getValue() + anosAFrente);
    }
}
