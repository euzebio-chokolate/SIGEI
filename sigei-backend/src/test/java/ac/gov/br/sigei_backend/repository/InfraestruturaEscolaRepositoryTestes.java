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
public class InfraestruturaEscolaRepositoryTestes {

 @Autowired
 InfraestruturaEscolaRepository infraestruturaEscolaRepository;

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
 private InfraestruturaEscola infra2025;
 private InfraestruturaEscola infra2026;

 @BeforeEach
 public void prepararCenario() {
  Municipio municipio = new Municipio();
  municipio.setNome("Cruzeiro do Sul");
  municipioRepository.save(municipio);

  TerraIndigena terraIndigena = new TerraIndigena();
  terraIndigena.setNome("Juruá");
  terraIndigena.setMunicipio(municipio);
  terraIndigenaRepository.save(terraIndigena);

  Povo povo = new Povo();
  povo.setNome("Ashaninka");
  povoRepository.save(povo);

  Aldeia aldeia = new Aldeia();
  aldeia.setNome("Apiwtxa");
  aldeia.setTerraIndigena(terraIndigena);
  aldeia.setPovo(povo);
  aldeiaRepository.save(aldeia);

  escola = new Escola();
  escola.setCodigo("77777");
  escola.setNome("Escola Indígena Infra");
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

  infra2025 = new InfraestruturaEscola();
  infra2025.setRegistroAnual(registroAnual2025);
  infraestruturaEscolaRepository.save(infra2025);

  infra2026 = new InfraestruturaEscola();
  infra2026.setRegistroAnual(registroAnual2026);
  infraestruturaEscolaRepository.save(infra2026);
 }

 @Test
 public void deveSalvarInfraestruturaEscola() {
  RegistroAnual novoRegistro = new RegistroAnual();
  novoRegistro.setEscola(escola);

  AnoReferencia ano2027 = new AnoReferencia();
  ano2027.setAno(2027);
  anoReferenciaRepository.save(ano2027);

  novoRegistro.setAnoReferencia(ano2027);
  registroAnualRepository.save(novoRegistro);

  InfraestruturaEscola novaInfra = new InfraestruturaEscola();
  novaInfra.setRegistroAnual(novoRegistro);

  infraestruturaEscolaRepository.save(novaInfra);

  assertNotEquals(null, novaInfra.getId());
 }

 @Test
 public void deveBuscarInfraestruturaEscolaPorId() {
  Optional<InfraestruturaEscola> infraBuscada = infraestruturaEscolaRepository.findById(infra2026.getId());

  assertTrue(infraBuscada.isPresent());
  assertEquals(infra2026.getId(), infraBuscada.get().getId());
 }

 @Test
 public void deveBuscarTopPorRegistroAnualEscolaOrderByAnoDesc() {
  Optional<InfraestruturaEscola> infraMaisRecente = infraestruturaEscolaRepository.findTopByRegistroAnualEscolaOrderByRegistroAnualAnoReferenciaAnoDesc(escola);

  assertTrue(infraMaisRecente.isPresent());
  assertEquals(infra2026.getId(), infraMaisRecente.get().getId());
 }

 @Test
 public void deveBuscarPorRegistroAnualId() {
  Optional<InfraestruturaEscola> infraBuscada = infraestruturaEscolaRepository.findByRegistroAnual_Id(registroAnual2025.getId());

  assertTrue(infraBuscada.isPresent());
  assertEquals(infra2025.getId(), infraBuscada.get().getId());
 }

 @Test
 public void deveVerificarSeExistsPorRegistroAnualId() {
  boolean existe = infraestruturaEscolaRepository.existsByRegistroAnual_Id(registroAnual2026.getId());

  assertTrue(existe);
 }

 @Test
 public void deveBuscarTopPorRegistroAnualEscolaComUnderscores() {
  Optional<InfraestruturaEscola> infraMaisRecente = infraestruturaEscolaRepository.findTopByRegistroAnual_EscolaOrderByRegistroAnual_AnoReferencia_AnoDesc(escola);

  assertTrue(infraMaisRecente.isPresent());
  assertEquals(infra2026.getId(), infraMaisRecente.get().getId());
 }

 @Test
 public void deveBuscarListaPorAnoReferenciaAno() {
  List<InfraestruturaEscola> infras = infraestruturaEscolaRepository.findByRegistroAnual_AnoReferencia_Ano(2026);

  assertFalse(infras.isEmpty());
  assertEquals(1, infras.size());
  assertEquals(infra2026.getId(), infras.get(0).getId());
 }
}