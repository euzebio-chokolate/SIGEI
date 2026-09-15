package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class AnoReferenciaRepositoryTest {

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Test
    public void deveSalvarAnoReferencia() {
        AnoReferencia anoReferencia = new AnoReferencia();
        anoReferencia.setAno(2026);

        anoReferenciaRepository.save(anoReferencia);

        assertNotEquals(null, anoReferencia.getId());
    }

    @Test
    public void deveBuscarAnoReferenciaPorId() {
        AnoReferencia anoReferencia = new AnoReferencia();
        anoReferencia.setAno(2026);

        anoReferenciaRepository.save(anoReferencia);

        Optional<AnoReferencia> anoBuscado = anoReferenciaRepository.findById(anoReferencia.getId());

        assertTrue(anoBuscado.isPresent());
        assertEquals(2026, anoBuscado.get().getAno());
    }

    @Test
    public void deveBuscarAnoReferenciaPorAno() {
        AnoReferencia anoReferencia = new AnoReferencia();
        anoReferencia.setAno(2027);

        anoReferenciaRepository.save(anoReferencia);

        Optional<AnoReferencia> anoBuscado = anoReferenciaRepository.findByAno(2027);

        assertTrue(anoBuscado.isPresent());
        assertEquals(2027, anoBuscado.get().getAno());
    }

    @Test
    public void deveBuscarTopAnoReferenciaOrdenadoDecrescente() {
        AnoReferencia anoReferencia1 = new AnoReferencia();
        anoReferencia1.setAno(2024);
        anoReferenciaRepository.save(anoReferencia1);

        AnoReferencia anoReferencia2 = new AnoReferencia();
        anoReferencia2.setAno(2028);
        anoReferenciaRepository.save(anoReferencia2);

        AnoReferencia anoReferencia3 = new AnoReferencia();
        anoReferencia3.setAno(2026);
        anoReferenciaRepository.save(anoReferencia3);

        Optional<AnoReferencia> anoMaisRecente = anoReferenciaRepository.findTopByOrderByAnoDesc();

        assertTrue(anoMaisRecente.isPresent());
        assertEquals(2028, anoMaisRecente.get().getAno());
    }
}