package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class TerraIndigenaRepositoryTestes {

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Test
    public void deveSalvarTerraIndigena() {
        Municipio municipio = new Municipio();
        municipio.setNome("Feijó");
        municipioRepository.save(municipio);

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Envira");
        terraIndigena.setMunicipio(municipio);

        terraIndigenaRepository.save(terraIndigena);

        assertNotEquals(null, terraIndigena.getId());
        assertEquals("Envira", terraIndigena.getNome());
    }

    @Test
    public void deveBuscarTerraIndigenaPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Tarauacá");
        municipioRepository.save(municipio);

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Rio Gregório");
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);

        Optional<TerraIndigena> terraBuscada = terraIndigenaRepository.findById(terraIndigena.getId());

        assertTrue(terraBuscada.isPresent());
        assertEquals("Rio Gregório", terraBuscada.get().getNome());
        assertEquals("Tarauacá", terraBuscada.get().getMunicipio().getNome());
    }
}