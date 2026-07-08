package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Povo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PovoRepositoryTestes {

    @Autowired
    PovoRepository povoRepository;

    @Test
    public void deveSalvarPovo() {
        Povo povo = new Povo();
        povo.setNome("Ashaninka");

        povoRepository.save(povo);

        assertNotNull(povo.getId());
        assertEquals("Ashaninka", povo.getNome());
    }

    @Test
    public void deveBuscarPovoPorId() {
        Povo povo = new Povo();
        povo.setNome("Huni Kuin");
        povoRepository.save(povo);

        Optional<Povo> povoBuscado = povoRepository.findById(povo.getId());

        assertTrue(povoBuscado.isPresent());
        assertEquals("Huni Kuin", povoBuscado.get().getNome());
    }
}