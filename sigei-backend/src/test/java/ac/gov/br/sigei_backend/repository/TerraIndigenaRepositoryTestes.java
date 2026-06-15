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
    MunicipioRepository municipioRepository; // Usado para criar a dependência

    @Test
    public void deveSalvarTerraIndigena() {
        // 1. Preparação (Arrange)
        // Criando e salvando um município primeiro para respeitar a chave estrangeira
        Municipio municipio = new Municipio();
        municipio.setNome("Feijó");
        municipioRepository.save(municipio);

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Envira");
        terraIndigena.setMunicipio(municipio);

        // 2. Ação (Act)
        terraIndigenaRepository.save(terraIndigena);

        // 3. Verificação (Assert)
        assertNotEquals(null, terraIndigena.getId());
        assertEquals("Envira", terraIndigena.getNome());
    }

    @Test
    public void deveBuscarTerraIndigenaPorId() {
        // 1. Preparação (Arrange)
        Municipio municipio = new Municipio();
        municipio.setNome("Tarauacá");
        municipioRepository.save(municipio);

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Rio Gregório");
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);

        // 2. Ação (Act)
        Optional<TerraIndigena> terraBuscada = terraIndigenaRepository.findById(terraIndigena.getId());

        // 3. Verificação (Assert)
        assertTrue(terraBuscada.isPresent());
        assertEquals("Rio Gregório", terraBuscada.get().getNome());
        assertEquals("Tarauacá", terraBuscada.get().getMunicipio().getNome());
    }
}