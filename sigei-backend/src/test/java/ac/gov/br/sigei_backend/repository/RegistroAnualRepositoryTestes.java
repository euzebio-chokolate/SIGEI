package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

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
    AldeiaRepository aldeiaRepository;

    @Autowired
    TerraIndigenaRepository terraIndigenaRepository;

    @Autowired
    MunicipioRepository municipioRepository;

    @Autowired
    PovoRepository povoRepository;

    @Test
    public void deveSalvarRegistroAnual() {
        Municipio municipio = new Municipio();
        municipio.setNome("Mâncio Lima");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra Indígena Nukini");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Povo povo = new Povo();
        povo.setNome("Nukini");
        povoRepository.save(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Nukini");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        Escola escola = new Escola();
        escola.setCodigo("554433");
        escola.setNome("Escola Nukini");
        escola.setTipoUnidade("Polo");
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAno(2024);
        registro.setRede("Estadual");
        registro.setLocalFuncionamento("Prédio Próprio");

        registroAnualRepository.save(registro);

        assertNotNull(registro.getId());
        assertEquals(2024, registro.getAno());
        assertEquals("Estadual", registro.getRede());
        assertEquals("Prédio Próprio", registro.getLocalFuncionamento());
    }

    @Test
    public void deveBuscarRegistroAnualPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Santa Rosa do Purus");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("998877");
        escola.setNome("Escola Santa Rosa");
        escola.setTipoUnidade("Anexo");
        escolaRepository.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAno(2025);
        registro.setRede("Municipal");
        registro.setLocalFuncionamento("Cedido");
        registroAnualRepository.save(registro);

        Optional<RegistroAnual> registroBuscado = registroAnualRepository.findById(registro.getId());

        assertTrue(registroBuscado.isPresent());
        assertEquals(2025, registroBuscado.get().getAno());
        assertEquals("Municipal", registroBuscado.get().getRede());
        assertEquals("Cedido", registroBuscado.get().getLocalFuncionamento());
    }
}