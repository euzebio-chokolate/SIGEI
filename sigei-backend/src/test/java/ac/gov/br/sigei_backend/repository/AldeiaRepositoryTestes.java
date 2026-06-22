package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
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
public class AldeiaRepositoryTestes {

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Test
    public void deveSalvarAldeiaComSucesso() {
        Municipio municipio = new Municipio();
        municipio.setNome("Feijó");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Envira");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Morada Nova");
        aldeia.setTerraIndigena(terra);

        aldeiaRepository.save(aldeia);

        assertNotNull(aldeia.getId(), "O ID não deveria ser nulo após guardar");
        assertEquals("Aldeia Morada Nova", aldeia.getNome());
        assertNotNull(aldeia.getTerraIndigena());
    }

    @Test
    public void deveBuscarAldeiaPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Tarauacá");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Rio Gregório");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Yawanawá");
        aldeia.setTerraIndigena(terra);
        aldeiaRepository.save(aldeia);

        Optional<Aldeia> aldeiaBuscada = aldeiaRepository.findById(aldeia.getId());

        assertTrue(aldeiaBuscada.isPresent(), "A aldeia deveria ter sido encontrada");
        assertEquals("Aldeia Yawanawá", aldeiaBuscada.get().getNome());
        assertEquals("Rio Gregório", aldeiaBuscada.get().getTerraIndigena().getNome());
    }
}