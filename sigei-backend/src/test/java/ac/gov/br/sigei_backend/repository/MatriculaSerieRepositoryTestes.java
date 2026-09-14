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
public class MatriculaSerieRepositoryTestes {

 @Autowired
 MatriculaSerieRepository matriculaSerieRepository;

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
 private MatriculaSerie matriculaSerie;

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

  matriculaSerie = new MatriculaSerie();
  matriculaSerie.setRegistro(registroAnual);
  matriculaSerie.setDenominacao("1º Ano");
  matriculaSerie.setTotal(25);
  matriculaSerieRepository.save(matriculaSerie);
 }

 @Test
 public void deveSalvarMatriculaSerie() {
  MatriculaSerie novaMatricula = new MatriculaSerie();
  novaMatricula.setRegistro(registroAnual);
  novaMatricula.setDenominacao("2º Ano");
  novaMatricula.setTotal(20);

  matriculaSerieRepository.save(novaMatricula);

  assertNotEquals(null, novaMatricula.getId());
 }

 @Test
 public void deveBuscarMatriculaSeriePorId() {
  Optional<MatriculaSerie> resultado = matriculaSerieRepository.findById(matriculaSerie.getId());

  assertTrue(resultado.isPresent());
  assertEquals(matriculaSerie.getId(), resultado.get().getId());
 }

 @Test
 public void deveFindByRegistro() {
  List<MatriculaSerie> resultado = matriculaSerieRepository.findByRegistro(registroAnual);

  assertFalse(resultado.isEmpty());
  assertEquals(1, resultado.size());
  assertEquals("1º Ano", resultado.get(0).getDenominacao());
 }

 @Test
 public void deveDeleteByRegistro() {
  matriculaSerieRepository.deleteByRegistro(registroAnual);

  List<MatriculaSerie> resultado = matriculaSerieRepository.findByRegistro(registroAnual);
  assertTrue(resultado.isEmpty());
 }
}