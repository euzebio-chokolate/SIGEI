package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
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
public class MatriculaNivelRepositoryTestes {

    @Autowired
    MatriculaNivelRepository matriculaRepository;

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
    public void deveSalvarMatriculaNivel() {
        Municipio municipio = new Municipio();
        municipio.setNome("Plácido de Castro");
        municipioRepository.save(municipio);

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra Nawa");
        terra.setMunicipio(municipio);
        terraIndigenaRepository.save(terra);

        Povo povo = new Povo();
        povo.setNome("Nawa");
        povoRepository.save(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia Nawa");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeiaRepository.save(aldeia);

        Escola escola = new Escola();
        escola.setCodigo("102030");
        escola.setNome("Escola Indígena Nawa");
        escola.setTipoUnidade("Polo");
        escola.setAldeia(aldeia);
        escolaRepository.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAno(2024);
        registro.setRede("Estadual");
        registro.setLocalFuncionamento("Próprio");
        registroAnualRepository.save(registro);

        MatriculaNivel matricula = new MatriculaNivel();
        matricula.setRegistro(registro);
        matricula.setNivel("Ensino Médio");
        matricula.setQtdMasculino(25);
        matricula.setQtdFeminino(30);
        matricula.setTotal(55);

        matriculaRepository.save(matricula);

        assertNotNull(matricula.getId());
        assertEquals("Ensino Médio", matricula.getNivel());
        assertEquals(55, matricula.getTotal());
    }

    @Test
    public void deveBuscarMatriculaNivelPorId() {
        Municipio municipio = new Municipio();
        municipio.setNome("Acrelândia");
        municipioRepository.save(municipio);

        Escola escola = new Escola();
        escola.setCodigo("405060");
        escola.setNome("Escola Acrelândia");
        escola.setTipoUnidade("Anexo");
        escolaRepository.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAno(2025);
        registro.setRede("Municipal");
        registro.setLocalFuncionamento("Alugado");
        registroAnualRepository.save(registro);

        MatriculaNivel matricula = new MatriculaNivel();
        matricula.setRegistro(registro);
        matricula.setNivel("Educação Infantil");
        matricula.setQtdMasculino(12);
        matricula.setQtdFeminino(18);
        matricula.setTotal(30);
        matriculaRepository.save(matricula);

        Optional<MatriculaNivel> matriculaBuscada = matriculaRepository.findById(matricula.getId());

        assertTrue(matriculaBuscada.isPresent());
        assertEquals("Educação Infantil", matriculaBuscada.get().getNivel());
        assertEquals(18, matriculaBuscada.get().getQtdFeminino());
    }
}