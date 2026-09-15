package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class MunicipioRepositoryTest {

    @Autowired
    MunicipioRepository municipioRepository;

    @Test
    public void deveSalvarMunicipio() {
        Municipio municipio = new Municipio();
        municipio.setNome("Rio Branco");

        municipioRepository.save(municipio);

        assertNotEquals(null, municipio.getId());
    }

    @Test
    public void deveBuscarMunicipioPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Cruzeiro do Sul");
        municipioRepository.save(municipio);

        Optional<Municipio> optionalMunicipio = municipioRepository.findById(municipio.getId());

        assertTrue(optionalMunicipio.isPresent());
        assertEquals("Cruzeiro do Sul", optionalMunicipio.get().getNome());
    }

    @Test
    public void deveBuscarMunicipioPorNome() {
        Municipio municipio = new Municipio();
        municipio.setNome("Sena Madureira");
        municipioRepository.save(municipio);

        Optional<Municipio> optionalMunicipio = municipioRepository.findByNome("Sena Madureira");

        assertNotEquals(null, optionalMunicipio.get());
        assertEquals("Sena Madureira", optionalMunicipio.get().getNome());
    }
}