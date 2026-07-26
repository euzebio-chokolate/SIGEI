package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
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
public class EscolaRepositoryTestes {

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Test
    public void deveSalvarEscolaComSucesso() {
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

        Escola escola = new Escola();
        escola.setCodigo("123456");
        escola.setNome("Escola Indígena Modelo");
        escola.setTipoUnidade("Polo");;
        escola.setAldeia(aldeia);

        escolaRepository.save(escola);

        assertNotNull(escola.getId());
        assertEquals("123456", escola.getCodigo());
        assertEquals("Escola Indígena Modelo", escola.getNome());
    }

    @Test
    public void deveBuscarEscolaPorCodigo() {
        Municipio municipio = new Municipio();
        municipio.setNome("Feijó");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("987654");
        escola.setNome("Escola Nova Esperança");
        escola.setTipoUnidade("Anexo");
        escolaRepository.save(escola);

        Optional<Escola> escolaBuscada = escolaRepository.findEscolaByCodigo("987654");

        assertTrue(escolaBuscada.isPresent());
        assertEquals("Escola Nova Esperança", escolaBuscada.get().getNome());
        assertEquals("987654", escolaBuscada.get().getCodigo());
    }

    @Test
    public void deveBuscarEscolaPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Cruzeiro do Sul");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("111222");
        escola.setNome("Escola Central");
        escola.setTipoUnidade("Sede");
        escolaRepository.save(escola);

        Optional<Escola> escolaBuscada = escolaRepository.findById(escola.getId());

        assertTrue(escolaBuscada.isPresent());
        assertEquals("Escola Central", escolaBuscada.get().getNome());
        assertEquals("Sede", escolaBuscada.get().getTipoUnidade());
    }
}