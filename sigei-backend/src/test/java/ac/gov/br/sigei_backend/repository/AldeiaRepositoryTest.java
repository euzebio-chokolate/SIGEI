package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class AldeiaRepositoryTest {

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    PovoRepository povoRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    private Municipio municipio;
    private TerraIndigena terraIndigena;
    private Povo povo;
    private Aldeia aldeia;

    @BeforeEach
    public void prepararCenario() {
        municipio = new Municipio();
        municipio.setNome("Tarauacá");
        municipioRepository.save(municipio);

        terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Rio Gregório");
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);

        povo = new Povo();
        povo.setNome("Yawanawá");
        povoRepository.save(povo);

        aldeia = new Aldeia();
        aldeia.setNome("Mutum");
        aldeia.setTerraIndigena(terraIndigena);
        aldeia.setPovo(povo);
    }

    @Test
    public void deveSalvarAldeia() {
        aldeiaRepository.save(aldeia);
        assertNotEquals(null, aldeia.getId());
    }

    @Test
    public void deveBuscarAldeiaPorId() {
        aldeiaRepository.save(aldeia);

        Optional<Aldeia> aldeiaBuscada = aldeiaRepository.findById(aldeia.getId());

        assertTrue(aldeiaBuscada.isPresent());
        assertEquals("Mutum", aldeiaBuscada.get().getNome());
    }

    @Test
    public void deveBuscarAldeiaPorNomeTerraEPovo() {
        aldeiaRepository.save(aldeia);

        Optional<Aldeia> aldeiaBuscada = aldeiaRepository.findByNomeIgnoreCaseAndTerraIndigenaAndPovo(
                "mutum", terraIndigena, povo
        );

        assertTrue(aldeiaBuscada.isPresent());
    }

    @Test
    public void deveVerificarSeAldeiaExiste() {
        aldeiaRepository.save(aldeia);

        boolean existe = aldeiaRepository.existsByNomeIgnoreCaseAndTerraIndigenaAndPovo(
                "MUTUM", terraIndigena, povo
        );

        assertTrue(existe);
    }

    @Test
    public void deveContarAldeiasPorMunicipioId() {
        aldeiaRepository.save(aldeia);

        long total = aldeiaRepository.countByTerraIndigena_Municipio_Id(municipio.getId());

        assertEquals(1L, total);
    }

    @Test
    public void deveContarAldeiasPorTerraIndigenaId() {
        aldeiaRepository.save(aldeia);

        long total = aldeiaRepository.countByTerraIndigena_Id(terraIndigena.getId());

        assertEquals(1L, total);
    }
}