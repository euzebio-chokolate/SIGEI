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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PovoRepositoryTestes {

    @Autowired
    PovoRepository povoRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    private Povo povo;

    @BeforeEach
    public void prepararCenario() {
        Municipio municipio = new Municipio();
        municipio.setNome("Rio Branco");
        municipioRepository.save(municipio);

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Kaxinawá");
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);

        povo = new Povo();
        povo.setNome("Huni Kuin");
        povoRepository.save(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Boa Vista");
        aldeia.setTerraIndigena(terraIndigena);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);
    }

    @Test
    public void deveSalvarPovo() {
        Povo novoPovo = new Povo();
        novoPovo.setNome("Ashaninka");

        povoRepository.save(novoPovo);

        assertNotEquals(null, novoPovo.getId());
    }

    @Test
    public void deveBuscarPovoPorId() {
        Optional<Povo> resultado = povoRepository.findById(povo.getId());

        assertTrue(resultado.isPresent());
        assertEquals(povo.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarTodosPovosComAldeias() {
        List<Povo> resultados = povoRepository.findAll();

        assertFalse(resultados.isEmpty());
        assertEquals("Huni Kuin", resultados.get(0).getNome());
    }

    @Test
    public void deveBuscarPorNomeIgnoreCase() {
        Optional<Povo> resultado = povoRepository.findByNomeIgnoreCase("huni kuin");

        assertTrue(resultado.isPresent());
        assertEquals("Huni Kuin", resultado.get().getNome());
    }

    @Test
    public void deveVerificarSeExistePorNomeIgnoreCase() {
        boolean existe = povoRepository.existsByNomeIgnoreCase("HUNI KUIN");

        assertTrue(existe);
    }
}