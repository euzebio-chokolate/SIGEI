package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardEvolucaoDTO;
import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardItemDTO;
import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardResumoDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.projection.DashboardInfraestruturaProjection;
import ac.gov.br.sigei_backend.repository.projection.DashboardProfessoresProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class DashboardRepositoryTest {

 @Autowired
 DashboardRepository dashboardRepository;

 @Autowired
 AnoReferenciaRepository anoReferenciaRepository;

 @Autowired
 MunicipioRepository municipioRepository;

 @Autowired
 TerraIndigenaRepository terraIndigenaRepository;

 @Autowired
 PovoRepository povoRepository;

 @Autowired
 AldeiaRepository aldeiaRepository;

 @Autowired
 EscolaRepository escolaRepository;

 @Autowired
 RegistroAnualRepository registroAnualRepository;

 @Autowired
 MatriculaNivelRepository matriculaNivelRepository;

 @Autowired
 InfraestruturaEscolaRepository infraestruturaEscolaRepository;

 @Autowired
 ProfessorRepository professorRepository;

 @Autowired
 LotacaoProfessorRepository lotacaoProfessorRepository;

 private Integer anoAtual = 2026;
 private AnoReferencia anoReferencia;
 private Municipio municipio;
 private Escola escola;

 @BeforeEach
 public void prepararCenario() {
  anoReferencia = new AnoReferencia();
  anoReferencia.setAno(anoAtual);
  anoReferenciaRepository.save(anoReferencia);

  municipio = new Municipio();
  municipio.setNome("Tarauacá");
  municipioRepository.save(municipio);

  TerraIndigena terraIndigena = new TerraIndigena();
  terraIndigena.setNome("Rio Gregório");
  terraIndigena.setMunicipio(municipio);
  terraIndigenaRepository.save(terraIndigena);

  Povo povo = new Povo();
  povo.setNome("Yawanawá");
  povoRepository.save(povo);

  Aldeia aldeia = new Aldeia();
  aldeia.setNome("Mutum");
  aldeia.setTerraIndigena(terraIndigena);
  aldeia.setPovo(povo);
  aldeiaRepository.save(aldeia);

  escola = new Escola();
  escola.setCodigo("123456");
  escola.setNome("Escola Yawanawá");
  escola.setAldeia(aldeia);
  escolaRepository.save(escola);

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setAnoReferencia(anoReferencia);
  registroAnual.setEscola(escola);
  registroAnual.setTotalAlunos(50);
  registroAnualRepository.save(registroAnual);

  MatriculaNivel matriculaNivel = new MatriculaNivel();
  matriculaNivel.setRegistro(registroAnual);
  matriculaNivel.setNivel("Ensino Fundamental");
  matriculaNivel.setTotal(50);
  matriculaNivelRepository.save(matriculaNivel);

  InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
  infraestrutura.setRegistroAnual(registroAnual);
  infraestrutura.setTemInternet(true);
  infraestrutura.setTipoEnergia("Solar");
  infraestrutura.setTemPredio(true);
  infraestrutura.setSituacao("Em funcionamento");
  infraestruturaEscolaRepository.save(infraestrutura);
 }

 @Test
 public void deveBuscarResumo() {
  DashboardResumoDTO resumo = dashboardRepository.buscarResumo(anoAtual);

  assertNotNull(resumo);
  assertEquals(1L, resumo.totalEscolas());
  assertEquals(50L, resumo.totalMatriculas());
  assertEquals(1L, resumo.totalMunicipios());
  assertEquals(1L, resumo.totalTerrasIndigenas());
 }

 @Test
 public void deveBuscarEvolucaoMatriculas() {
  List<DashboardEvolucaoDTO> evolucao = dashboardRepository.buscarEvolucaoMatriculas();

  assertFalse(evolucao.isEmpty());
  assertEquals(anoAtual, evolucao.get(0).ano());
  assertEquals(50L, evolucao.get(0).total());
 }

 @Test
 public void deveBuscarEvolucaoEscolas() {
  List<DashboardEvolucaoDTO> evolucao = dashboardRepository.buscarEvolucaoEscolas();

  assertFalse(evolucao.isEmpty());
  assertEquals(anoAtual, evolucao.get(0).ano());
  assertEquals(1L, evolucao.get(0).total());
 }

 @Test
 public void deveBuscarMatriculasPorNivel() {
  List<DashboardItemDTO> matriculas = dashboardRepository.buscarMatriculasPorNivel(anoAtual);

  assertFalse(matriculas.isEmpty());
  assertEquals("Ensino Fundamental", matriculas.get(0).descricao());
  assertEquals(50L, matriculas.get(0).total());
 }

 @Test
 public void deveBuscarInfraestrutura() {
  DashboardInfraestruturaProjection infraestrutura = dashboardRepository.buscarInfraestrutura(anoAtual);

  assertNotNull(infraestrutura);
  assertEquals(1L, infraestrutura.getTotalAvaliado());
  assertEquals(1L, infraestrutura.getComInternet());
  assertEquals(1L, infraestrutura.getComEnergia());
  assertEquals(1L, infraestrutura.getComPredio());
 }

 @Test
 public void deveBuscarEscolasPorMunicipio() {
  List<DashboardItemDTO> escolasPorMunicipio = dashboardRepository.buscarEscolasPorMunicipio(anoAtual);

  assertFalse(escolasPorMunicipio.isEmpty());
  assertEquals("Tarauacá", escolasPorMunicipio.get(0).descricao());
  assertEquals(1L, escolasPorMunicipio.get(0).total());
 }

 @Test
 public void deveBuscarSituacaoEscolas() {
  List<DashboardItemDTO> situacaoEscolas = dashboardRepository.buscarSituacaoEscolas(anoAtual);

  assertFalse(situacaoEscolas.isEmpty());
  assertEquals("Em funcionamento", situacaoEscolas.get(0).descricao());
  assertEquals(1L, situacaoEscolas.get(0).total());
 }

 @Test
 public void deveBuscarProfessores() {
  Professor professor = new Professor();
  professor.setNome("Maria Yawanawá");
  professorRepository.save(professor);

  lotacao(professor, escola, "EFETIVO");
  lotacao(professor, null, "PSS");
  lotacao(null, escola, "PSS");

  DashboardProfessoresProjection resumo = dashboardRepository.buscarProfessores(anoAtual);

  assertEquals(1L, resumo.getTotalProfessores());
  assertEquals(3L, resumo.getTotalLotacoes());
  assertEquals(1L, resumo.getVagasEmAberto());
  assertEquals(1L, resumo.getLotacoesNoNucleo());

  List<DashboardItemDTO> porContrato = dashboardRepository.buscarLotacoesPorTipoContrato(anoAtual);
  assertEquals("PSS", porContrato.get(0).descricao());
  assertEquals(2L, porContrato.get(0).total());
 }

 private void lotacao(Professor professor, Escola escolaLotacao, String tipoContrato) {
  LotacaoProfessor lotacao = new LotacaoProfessor();
  lotacao.setAnoReferencia(anoReferencia);
  lotacao.setProfessor(professor);
  lotacao.setEscola(escolaLotacao);
  lotacao.setMunicipio(municipio);
  lotacao.setTipoContrato(tipoContrato);
  lotacaoProfessorRepository.save(lotacao);
 }
}