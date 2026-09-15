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
public class EquipeEscolaRepositoryTest {

 @Autowired
 EquipeEscolaRepository equipeEscolaRepository;

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
 private RegistroAnual registroAnual2025;
 private RegistroAnual registroAnual2026;
 private EquipeEscola equipe2025;
 private EquipeEscola equipe2026;

 @BeforeEach
 public void prepararCenario() {
  Municipio municipio = new Municipio();
  municipio.setNome("Feijó");
  municipioRepository.save(municipio);

  TerraIndigena terraIndigena = new TerraIndigena();
  terraIndigena.setNome("Envira");
  terraIndigena.setMunicipio(municipio);
  terraIndigenaRepository.save(terraIndigena);

  Povo povo = new Povo();
  povo.setNome("Shanawdawa");
  povoRepository.save(povo);

  Aldeia aldeia = new Aldeia();
  aldeia.setNome("Nova Vida");
  aldeia.setTerraIndigena(terraIndigena);
  aldeia.setPovo(povo);
  aldeiaRepository.save(aldeia);

  escola = new Escola();
  escola.setCodigo("99999");
  escola.setNome("Escola Teste");
  escola.setAldeia(aldeia);
  escolaRepository.save(escola);

  AnoReferencia ano2025 = new AnoReferencia();
  ano2025.setAno(2025);
  anoReferenciaRepository.save(ano2025);

  AnoReferencia ano2026 = new AnoReferencia();
  ano2026.setAno(2026);
  anoReferenciaRepository.save(ano2026);

  registroAnual2025 = new RegistroAnual();
  registroAnual2025.setEscola(escola);
  registroAnual2025.setAnoReferencia(ano2025);
  registroAnualRepository.save(registroAnual2025);

  registroAnual2026 = new RegistroAnual();
  registroAnual2026.setEscola(escola);
  registroAnual2026.setAnoReferencia(ano2026);
  registroAnualRepository.save(registroAnual2026);

  equipe2025 = new EquipeEscola();
  equipe2025.setRegistroAnual(registroAnual2025);
  equipeEscolaRepository.save(equipe2025);

  equipe2026 = new EquipeEscola();
  equipe2026.setRegistroAnual(registroAnual2026);
  equipeEscolaRepository.save(equipe2026);
 }

 @Test
 public void deveSalvarEquipeEscola() {
  RegistroAnual novoRegistro = new RegistroAnual();
  novoRegistro.setEscola(escola);

  AnoReferencia ano2027 = new AnoReferencia();
  ano2027.setAno(2027);
  anoReferenciaRepository.save(ano2027);

  novoRegistro.setAnoReferencia(ano2027);
  registroAnualRepository.save(novoRegistro);

  EquipeEscola novaEquipe = new EquipeEscola();
  novaEquipe.setRegistroAnual(novoRegistro);

  equipeEscolaRepository.save(novaEquipe);

  assertNotEquals(null, novaEquipe.getId());
 }

 @Test
 public void deveBuscarEquipeEscolaPorId() {
  Optional<EquipeEscola> equipeBuscada = equipeEscolaRepository.findById(equipe2026.getId());

  assertTrue(equipeBuscada.isPresent());
  assertEquals(equipe2026.getId(), equipeBuscada.get().getId());
 }

 @Test
 public void deveBuscarTopPorRegistroAnualEscolaOrderByAnoDesc() {
  Optional<EquipeEscola> equipeMaisRecente = equipeEscolaRepository.findTopByRegistroAnualEscolaOrderByRegistroAnualAnoReferenciaAnoDesc(escola);

  assertTrue(equipeMaisRecente.isPresent());
  assertEquals(equipe2026.getId(), equipeMaisRecente.get().getId());
 }

 @Test
 public void deveBuscarPorRegistroAnualId() {
  Optional<EquipeEscola> equipeBuscada = equipeEscolaRepository.findByRegistroAnual_Id(registroAnual2025.getId());

  assertTrue(equipeBuscada.isPresent());
  assertEquals(equipe2025.getId(), equipeBuscada.get().getId());
 }

 @Test
 public void deveVerificarSeExistsPorRegistroAnualId() {
  boolean existe = equipeEscolaRepository.existsByRegistroAnual_Id(registroAnual2026.getId());

  assertTrue(existe);
 }

 @Test
 public void deveBuscarTopPorRegistroAnualEscolaComUnderscores() {
  Optional<EquipeEscola> equipeMaisRecente = equipeEscolaRepository.findTopByRegistroAnual_EscolaOrderByRegistroAnual_AnoReferencia_AnoDesc(escola);

  assertTrue(equipeMaisRecente.isPresent());
  assertEquals(equipe2026.getId(), equipeMaisRecente.get().getId());
 }

 @Test
 public void deveBuscarListaPorAnoReferenciaAno() {
  List<EquipeEscola> equipes = equipeEscolaRepository.findByRegistroAnual_AnoReferencia_Ano(2026);

  assertFalse(equipes.isEmpty());
  assertEquals(1, equipes.size());
  assertEquals(equipe2026.getId(), equipes.get(0).getId());
 }
}