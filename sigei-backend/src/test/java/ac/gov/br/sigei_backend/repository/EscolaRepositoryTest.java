package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.*;
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
public class EscolaRepositoryTest {

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    PovoRepository povoRepository;

    private Escola escola;
    private Aldeia aldeia;

    @BeforeEach
    public void prepararCenario() {
        Municipio municipio = new Municipio();
        municipio.setNome("Rio Branco");
        municipioRepository.save(municipio);

        TerraIndigena terraIndigena = new TerraIndigena();
        terraIndigena.setNome("Kaxinawá");
        terraIndigena.setMunicipio(municipio);
        terraIndigenaRepository.save(terraIndigena);

        Povo povo = new Povo();
        povo.setNome("Huni Kuin");
        povoRepository.save(povo);

        aldeia = new Aldeia();
        aldeia.setNome("Boa Vista");
        aldeia.setTerraIndigena(terraIndigena);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        escola = new Escola();
        escola.setCodigo("12345678");
        escola.setNome("Escola Indígena");
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);
    }

    @Test
    public void deveSalvarEscola() {
        Aldeia outraAldeia = new Aldeia();
        outraAldeia.setNome("Aldeia Nova");
        outraAldeia.setTerraIndigena(aldeia.getTerraIndigena());
        outraAldeia.setPovo(aldeia.getPovo());
        aldeiaRepository.save(outraAldeia);

        Escola novaEscola = new Escola();
        novaEscola.setCodigo("87654321");
        novaEscola.setNome("Escola Nova");
        novaEscola.setAldeia(outraAldeia);

        escolaRepository.save(novaEscola);

        assertNotEquals(null, novaEscola.getId());
    }

    @Test
    public void deveBuscarEscolaPorId() {
        Optional<Escola> resultado = escolaRepository.findById(escola.getId());

        assertTrue(resultado.isPresent());
        assertEquals(escola.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarTodasPorCodigo() {
        List<Escola> resultado = escolaRepository.findAllByCodigo("12345678");

        assertFalse(resultado.isEmpty());
        assertEquals("12345678", resultado.get(0).getCodigo());
    }

    @Test
    public void deveBuscarPrimeiraPorCodigoETipoUnidade() {
        Optional<Escola> resultado = escolaRepository.findFirstByCodigoAndTipoUnidadeIgnoreCase("12345678", null);

        assertTrue(resultado.isPresent());
        assertEquals("12345678", resultado.get().getCodigo());
    }

    @Test
    public void deveBuscarPrimeiraPorCodigoENome() {
        Optional<Escola> resultado = escolaRepository.findFirstByCodigoAndNomeIgnoreCase("12345678", "escola indígena");

        assertTrue(resultado.isPresent());
        assertEquals("12345678", resultado.get().getCodigo());
    }

    @Test
    public void deveBuscarPrimeiraPorCodigoENomeAldeia() {
        Optional<Escola> resultado = escolaRepository.findFirstByCodigoAndAldeia_NomeIgnoreCase("12345678", "boa vista");

        assertTrue(resultado.isPresent());
        assertEquals("12345678", resultado.get().getCodigo());
    }

    @Test
    public void deveVerificarSeExistePorCodigoENome() {
        boolean existe = escolaRepository.existsByCodigoAndNomeIgnoreCase("12345678", "ESCOLA INDÍGENA");

        assertTrue(existe);
    }

    @Test
    public void deveContarEscolasPorMunicipioId() {
        long total = escolaRepository.countByAldeia_TerraIndigena_Municipio_Id(escola.getAldeia().getTerraIndigena().getMunicipio().getId());

        assertEquals(1L, total);
    }

    @Test
    public void deveContarEscolasPorTerraIndigenaId() {
        long total = escolaRepository.countByAldeia_TerraIndigena_Id(escola.getAldeia().getTerraIndigena().getId());

        assertEquals(1L, total);
    }
}