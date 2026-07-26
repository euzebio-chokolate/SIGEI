package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
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
public class MatriculaSerieRepositoryTestes {

    @Autowired
    MatriculaSerieRepository matriculaSerieRepository;

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
    public void deveSalvarMatriculaSerie() {
        Municipio municipio = new Municipio();
        municipio.setNome("Bujari");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra Apolima-Arara");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Povo povo = new Povo();
        povo.setNome("Apolima-Arara");
        povoRepository.save(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Central");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        Escola escola = new Escola();
        escola.setCodigo("112233");
        escola.setNome("Escola Indígena Apolima");
        escola.setTipoUnidade("Polo");
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAno(2024);
        registro.setRede("Estadual");
        registro.setLocalFuncionamento("Próprio");
        registroAnualRepository.save(registro);

        MatriculaSerie matricula = new MatriculaSerie();
        matricula.setRegistro(registro);
        matricula.setDenominacao("1º Ano");
        matricula.setEtapaEnsino("Ensino Fundamental I");
        matricula.setQtdMasculino(15);
        matricula.setQtdFeminino(20);

        matriculaSerieRepository.save(matricula);

        assertNotNull(matricula.getId());
        assertEquals("1º Ano", matricula.getDenominacao());
        assertEquals(15, matricula.getQtdMasculino());
    }

    @Test
    public void deveBuscarMatriculaSeriePorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Capixaba");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("445566");
        escola.setNome("Escola Capixaba");
        escola.setTipoUnidade("Anexo");
        escolaRepository.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAno(2025);
        registro.setRede("Municipal");
        registro.setLocalFuncionamento("Alugado");
        registroAnualRepository.save(registro);

        MatriculaSerie matricula = new MatriculaSerie();
        matricula.setRegistro(registro);
        matricula.setDenominacao("2º Ano");
        matricula.setEtapaEnsino("Ensino Fundamental I");
        matricula.setQtdMasculino(12);
        matricula.setQtdFeminino(18);
        matriculaSerieRepository.save(matricula);

        Optional<MatriculaSerie> matriculaBuscada = matriculaSerieRepository.findById(matricula.getId());

        assertTrue(matriculaBuscada.isPresent());
        assertEquals("2º Ano", matriculaBuscada.get().getDenominacao());
        assertEquals(18, matriculaBuscada.get().getQtdFeminino());
    }
}