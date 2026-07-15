package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class InfraestruturaEscolaRepositoryTestes {

    @Autowired
    InfraestruturaEscolaRepository infraestruturaRepository;

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
    public void deveSalvarInfraestrutura() {
        Municipio municipio = new Municipio();
        municipio.setNome("Tarauacá");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra de Tarauacá");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Povo povo = new Povo();
        povo.setNome("Yawanawá");
        povoRepository.save(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Central");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        Escola escola = new Escola();
        escola.setCodigo("999888");
        escola.setNome("Escola Yawanawá");
        escola.setTipoUnidade("Polo");
        escola.setMunicipio(municipio);
        escola.setTerraIndigena(terra);
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);

        InfraestruturaEscola infra = new InfraestruturaEscola();
        infra.setEscola(escola);
        infra.setAnoReferencia(2023);
        infra.setAnoConstrucao(2015);
        infra.setNumSalas(4);
        infra.setLatitude(-8.16);
        infra.setLongitude(-70.76);
        infra.setSituacao("Boa");
        infra.setTipoAcesso("Fluvial");
        infra.setTemPredio(true);
        infra.setTemAgua(true);
        infra.setTemInternet(false);
        infra.setTemEnergia(true);
        infra.setTemRegistroFotografico(true);

        infraestruturaRepository.save(infra);

        assertNotNull(infra.getId());
        assertEquals(2023, infra.getAnoReferencia());
        assertEquals("Fluvial", infra.getTipoAcesso());
        assertTrue(infra.getTemEnergia());
    }

    @Test
    public void deveBuscarInfraestruturaPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Feijó");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("111333");
        escola.setNome("Escola Feijó");
        escola.setTipoUnidade("Sede");
        escola.setMunicipio(municipio);
        escolaRepository.save(escola);

        InfraestruturaEscola infra = new InfraestruturaEscola();
        infra.setEscola(escola);
        infra.setAnoReferencia(2024);
        infra.setAnoConstrucao(2018);
        infra.setNumSalas(6);
        infra.setSituacao("Excelente");
        infra.setTipoAcesso("Terrestre");
        infraestruturaRepository.save(infra);

        Optional<InfraestruturaEscola> infraBuscada = infraestruturaRepository.findById(infra.getId());

        assertTrue(infraBuscada.isPresent());
        assertEquals(2024, infraBuscada.get().getAnoReferencia());
        assertEquals("Excelente", infraBuscada.get().getSituacao());
    }
}