package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.models.Povo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class EquipeEscolaRepositoryTestes {

    @Autowired
    EquipeEscolaRepository equipeEscolaRepository;

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

    @Test
    public void deveSalvarEquipeEscola() {
        Municipio municipio = new Municipio();
        municipio.setNome("Jordão");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra do Jordão");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Povo povo = new Povo();
        povo.setNome("Huni Kuin");
        povoRepository.save(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Sede");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        Escola escola = new Escola();
        escola.setCodigo("445566");
        escola.setNome("Escola Jordão");
        escola.setTipoUnidade("Polo");
        escola.setMunicipio(municipio);
        escola.setTerraIndigena(terra);
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);

        EquipeEscola equipe = new EquipeEscola();
        equipe.setEscola(escola);
        equipe.setAnoReferencia(2024);
        equipe.setTemProfessorResponsavel(true);
        equipe.setTemEquipeGestora(false);

        equipeEscolaRepository.save(equipe);

        assertNotNull(equipe.getId());
        assertEquals(2024, equipe.getAnoReferencia());
        assertTrue(equipe.isTemProfessorResponsavel());
        assertFalse(equipe.isTemEquipeGestora());
    }

    @Test
    public void deveBuscarEquipeEscolaPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Porto Walter");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("778899");
        escola.setNome("Escola Porto Walter");
        escola.setTipoUnidade("Anexo");
        escola.setMunicipio(municipio);
        escolaRepository.save(escola);

        EquipeEscola equipe = new EquipeEscola();
        equipe.setEscola(escola);
        equipe.setAnoReferencia(2025);
        equipe.setTemZeladora(true);
        equipe.setTemMerendeira(true);
        equipeEscolaRepository.save(equipe);

        Optional<EquipeEscola> equipeBuscada = equipeEscolaRepository.findById(equipe.getId());

        assertTrue(equipeBuscada.isPresent());
        assertEquals(2025, equipeBuscada.get().getAnoReferencia());
        assertTrue(equipeBuscada.get().isTemZeladora());
    }
}