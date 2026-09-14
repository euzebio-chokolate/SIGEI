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
public class RegistroAnualRepositoryTestes {

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    PovoRepository povoRepository;

    private Escola escola;
    private AnoReferencia anoReferencia2025;
    private AnoReferencia anoReferencia2026;
    private RegistroAnual registroAnual2025;
    private RegistroAnual registroAnual2026;

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

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Boa Vista");
        aldeia.setTerraIndigena(terraIndigena);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        escola = new Escola();
        escola.setCodigo("12345678");
        escola.setNome("Escola Indígena");
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);

        anoReferencia2025 = new AnoReferencia();
        anoReferencia2025.setAno(2025);
        anoReferenciaRepository.save(anoReferencia2025);

        anoReferencia2026 = new AnoReferencia();
        anoReferencia2026.setAno(2026);
        anoReferenciaRepository.save(anoReferencia2026);

        registroAnual2025 = new RegistroAnual();
        registroAnual2025.setEscola(escola);
        registroAnual2025.setAnoReferencia(anoReferencia2025);
        registroAnualRepository.save(registroAnual2025);

        registroAnual2026 = new RegistroAnual();
        registroAnual2026.setEscola(escola);
        registroAnual2026.setAnoReferencia(anoReferencia2026);
        registroAnualRepository.save(registroAnual2026);
    }

    @Test
    public void deveSalvarRegistroAnual() {
        AnoReferencia ano2027 = new AnoReferencia();
        ano2027.setAno(2027);
        anoReferenciaRepository.save(ano2027);

        RegistroAnual novoRegistro = new RegistroAnual();
        novoRegistro.setEscola(escola);
        novoRegistro.setAnoReferencia(ano2027);
        registroAnualRepository.save(novoRegistro);

        assertNotEquals(null, novoRegistro.getId());
    }

    @Test
    public void deveBuscarPorId() {
        Optional<RegistroAnual> resultado = registroAnualRepository.findById(registroAnual2026.getId());

        assertTrue(resultado.isPresent());
        assertEquals(registroAnual2026.getId(), resultado.get().getId());
    }

    @Test
    public void deveVerificarSeExistePorEscolaEAnoReferencia() {
        boolean existe = registroAnualRepository.existsByEscolaAndAnoReferencia(escola, anoReferencia2026);

        assertTrue(existe);
    }

    @Test
    public void deveBuscarTopPorEscolaOrdenadoPorAnoDesc() {
        Optional<RegistroAnual> resultado = registroAnualRepository.findTopByEscolaOrderByAnoReferencia_AnoDesc(escola);

        assertTrue(resultado.isPresent());
        assertEquals(registroAnual2026.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarPorEscolaEAnoReferencia() {
        Optional<RegistroAnual> resultado = registroAnualRepository.findByEscolaAndAnoReferencia(escola, anoReferencia2025);

        assertTrue(resultado.isPresent());
        assertEquals(registroAnual2025.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarPorEscolaEAnoReferenciaAno() {
        Optional<RegistroAnual> resultado = registroAnualRepository.findByEscolaAndAnoReferencia_Ano(escola, 2026);

        assertTrue(resultado.isPresent());
        assertEquals(registroAnual2026.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarListaPorAnoReferenciaAno() {
        List<RegistroAnual> resultados = registroAnualRepository.findByAnoReferencia_Ano(2026);

        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
        assertEquals(registroAnual2026.getId(), resultados.get(0).getId());
    }
}