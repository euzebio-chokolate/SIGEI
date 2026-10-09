package ac.gov.br.sigei_backend.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfUtilsTest {

    @Test
    void deveGravarSoOsDigitos() {
        assertEquals("52998224725", CpfUtils.normalizar("529.982.247-25"));
        assertEquals("52998224725", CpfUtils.normalizar(" 52998224725 "));
    }

    @Test
    void vazioDeveVirarNulo() {
        assertNull(CpfUtils.normalizar(null));
        assertNull(CpfUtils.normalizar("  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-24", "111.111.111-11", "1234567890", "abc.def.ghi-jk", "529982247250"})
    void deveRecusarCpfInvalidoSemRepetirOValor(String cpf) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> CpfUtils.normalizar(cpf));

        assertEquals("CPF inválido.", erro.getMessage());
    }
}
