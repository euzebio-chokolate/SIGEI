package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
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
public class TerraIndigenaRepositoryTestes {

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    private Municipio municipio;
    private TerraIndigena terraIndigena;

    @BeforeEach
    public void prepararCenario() {
        municipio = new Municipio();
        municipio.setNome("Rio Branco");
        municipioRepository.save(municipio);

        terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Kaxinawá");
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);
    }

    @Test
    public void deveSalvarTerraIndigena() {
        TerraIndigena novaTerra = new TerraIndigena();
        novaTerra.setNome("Juruá");
        novaTerra.setMunicipio(municipio);

        terraIndigenaRepository.save(novaTerra);

        assertNotEquals(null, novaTerra.getId());
    }

    @Test
    public void deveBuscarTerraIndigenaPorId() {
        Optional<TerraIndigena> resultado = terraIndigenaRepository.findById(terraIndigena.getId());

        assertTrue(resultado.isPresent());
        assertEquals(terraIndigena.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarPorNomeIgnoreCaseEMunicipio() {
        Optional<TerraIndigena> resultado = terraIndigenaRepository.findByNomeIgnoreCaseAndMunicipio("kaxinawá", municipio);

        assertTrue(resultado.isPresent());
        assertEquals("Kaxinawá", resultado.get().getNome());
    }

    @Test
    public void deveVerificarSeExistePorNomeIgnoreCaseEMunicipio() {
        boolean existe = terraIndigenaRepository.existsByNomeIgnoreCaseAndMunicipio("KAXINAWÁ", municipio);

        assertTrue(existe);
    }

    @Test
    public void deveContarPorMunicipioId() {
        long total = terraIndigenaRepository.countByMunicipio_Id(municipio.getId());

        assertEquals(1L, total);
    }
}