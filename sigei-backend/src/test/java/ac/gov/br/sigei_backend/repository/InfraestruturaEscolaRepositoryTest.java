package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
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
public class InfraestruturaEscolaRepositoryTest {

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

 @Autowired
 EntityManager entityManager;

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

 @Test
 public void deveConsultarDadosDoMapaSemCarregarEntidadesRelacionadas() {
  infra2026.setLatitude(-9.9);
  infra2026.setLongitude(-67.8);
  infra2026.setTemInternet(true);
  infra2026.setFotoUrl("https://example.com/foto.jpg");
  entityManager.flush();
  entityManager.clear();
  var statistics = entityManager.getEntityManagerFactory()
          .unwrap(SessionFactory.class).getStatistics();
  statistics.setStatisticsEnabled(true);
  statistics.clear();
  try {
   var resultado = infraestruturaEscolaRepository.buscarMaisRecentesPorEscola();
   assertEquals(1, resultado.size());
   var item = resultado.get(0);
   assertEquals(infra2026.getId(), item.id());
   assertEquals(escola.getNome(), item.escola().nome());
   assertEquals(2026, item.anoReferencia().ano());
   assertEquals(-9.9, item.latitude());
   assertEquals(-67.8, item.longitude());
   assertTrue(item.temInternet());
   assertNull(item.numSalas());
   assertEquals("https://example.com/foto.jpg", item.fotoUrl());
   assertEquals(1, statistics.getPrepareStatementCount(),
           "O mapa deve buscar os campos da resposta em uma consulta, sem consultas por relacionamento");
   assertEquals(0, statistics.getEntityLoadCount(),
           "O mapa não precisa materializar entidades e seus relacionamentos EAGER");
  } finally {
   statistics.setStatisticsEnabled(false);
  }
 }

 @Test
 public void deveBuscarUmaInfraestruturaPorEscolaDoAnoMaisRecente() {
  // Outra escola: infraestrutura só em 2025 e um registro de 2027 ainda sem infraestrutura
  Escola outraEscola = new Escola();
  outraEscola.setCodigo("88888");
  outraEscola.setNome("Outra escola");
  escolaRepository.save(outraEscola);

  RegistroAnual outro2025 = new RegistroAnual();
  outro2025.setEscola(outraEscola);
  outro2025.setAnoReferencia(registroAnual2025.getAnoReferencia());
  registroAnualRepository.save(outro2025);

  InfraestruturaEscola outraInfra2025 = new InfraestruturaEscola();
  outraInfra2025.setRegistroAnual(outro2025);
  infraestruturaEscolaRepository.save(outraInfra2025);

  AnoReferencia ano2027 = new AnoReferencia();
  ano2027.setAno(2027);
  anoReferenciaRepository.save(ano2027);

  RegistroAnual outro2027 = new RegistroAnual();
  outro2027.setEscola(outraEscola);
  outro2027.setAnoReferencia(ano2027);
  registroAnualRepository.save(outro2027);

  List<Long> ids = infraestruturaEscolaRepository.buscarMaisRecentesPorEscola()
          .stream()
          .map(InfraestruturaEscolaResponseDTO::id)
          .sorted()
          .toList();

  assertEquals(List.of(infra2026.getId(), outraInfra2025.getId()).stream().sorted().toList(), ids);
 }
}
