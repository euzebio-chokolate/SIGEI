package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class MatriculaNivelRepositoryTest {

 @Autowired
 MatriculaNivelRepository matriculaNivelRepository;

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

 private RegistroAnual registroAnual;
 private MatriculaNivel matriculaNivel;

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

  Escola escola = new Escola();
  escola.setCodigo("12345678");
  escola.setNome("Escola Indígena");
  escola.setAldeia(aldeia);
  escolaRepository.save(escola);

  AnoReferencia anoReferencia = new AnoReferencia();
  anoReferencia.setAno(2026);
  anoReferenciaRepository.save(anoReferencia);

  registroAnual = new RegistroAnual();
  registroAnual.setEscola(escola);
  registroAnual.setAnoReferencia(anoReferencia);
  registroAnualRepository.save(registroAnual);

  matriculaNivel = new MatriculaNivel();
  matriculaNivel.setRegistro(registroAnual);
  matriculaNivel.setNivel("Ensino Fundamental");
  matriculaNivel.setTotal(30);
  matriculaNivelRepository.save(matriculaNivel);
 }

 @Test
 public void deveSalvarMatriculaNivel() {
  MatriculaNivel novaMatricula = new MatriculaNivel();
  novaMatricula.setRegistro(registroAnual);
  novaMatricula.setNivel("Ensino Médio");
  novaMatriculaNivelRepositorySave:
  matriculaNivelRepository.save(novaMatricula);

  assertNotEquals(null, novaMatricula.getId());
 }

 @Test
 public void deveFindByRegistro() {
  List<MatriculaNivel> resultado = matriculaNivelRepository.findByRegistro(registroAnual);

  assertFalse(resultado.isEmpty());
  assertEquals(1, resultado.size());
  assertEquals("Ensino Fundamental", resultado.get(0).getNivel());
 }

 @Test
 public void deveDeleteByRegistro() {
  matriculaNivelRepository.deleteByRegistro(registroAnual);

  List<MatriculaNivel> resultado = matriculaNivelRepository.findByRegistro(registroAnual);
  assertTrue(resultado.isEmpty());
 }
}