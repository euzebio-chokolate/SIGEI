package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Anexo;
import ac.gov.br.sigei_backend.models.Escola;
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
public class AnexoRepositoryTestes {

    @Autowired
    AnexoRepository anexoRepository;

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
    public void deveSalvarAnexo() {
        Municipio municipio = new Municipio();
        municipio.setNome("Sena Madureira");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra Sena");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Povo povo = new Povo();
        povo.setNome("Jaminawa");
        povoRepository.save(povo);

        Aldeia aldeia1 = new Aldeia();
        aldeia1.setNome("Aldeia Sede");
        aldeia1.setTerraIndigena(terra);
        aldeia1.setPovo(povo);
        aldeiaRepository.save(aldeia1);

        Aldeia aldeia2 = new Aldeia();
        aldeia2.setNome("Aldeia Anexa");
        aldeia2.setTerraIndigena(terra);
        aldeia2.setPovo(povo);
        aldeiaRepository.save(aldeia2);

        Escola escolaSede = new Escola();
        escolaSede.setCodigo("111111");
        escolaSede.setNome("Escola Sede");
        escolaSede.setTipoUnidade("Polo");
        escolaSede.setAldeia(aldeia1);
        escolaRepository.save(escolaSede);

        Escola escolaAnexa = new Escola();
        escolaAnexa.setCodigo("222222");
        escolaAnexa.setNome("Escola Anexa");
        escolaAnexa.setTipoUnidade("Anexo");
        escolaAnexa.setAldeia(aldeia2);
        escolaRepository.save(escolaAnexa);

        Anexo anexo = new Anexo();
        anexo.setEscola(escolaSede);
        anexo.setEscolaAnexo(escolaAnexa);

        anexoRepository.save(anexo);

        assertNotNull(anexo.getId());
    }

    @Test
    public void deveBuscarAnexoPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Manoel Urbano");
        municipioRepository.save(municipio);

        Escola escolaSede = new Escola();
        escolaSede.setCodigo("333333");
        escolaSede.setNome("Escola Polo Manoel Urbano");
        escolaSede.setTipoUnidade("Polo");
        escolaRepository.save(escolaSede);

        Escola escolaAnexa = new Escola();
        escolaAnexa.setCodigo("444444");
        escolaAnexa.setNome("Escola Anexa Manoel Urbano");
        escolaAnexa.setTipoUnidade("Anexo");
        escolaRepository.save(escolaAnexa);

        Anexo anexo = new Anexo();
        anexo.setEscola(escolaSede);
        anexo.setEscolaAnexo(escolaAnexa);
        anexoRepository.save(anexo);

        Optional<Anexo> anexoBuscado = anexoRepository.findById(anexo.getId());

        assertTrue(anexoBuscado.isPresent());
        assertEquals("333333", anexoBuscado.get().getEscola().getCodigo());
        assertEquals("444444", anexoBuscado.get().getEscolaAnexo().getCodigo());
    }
}