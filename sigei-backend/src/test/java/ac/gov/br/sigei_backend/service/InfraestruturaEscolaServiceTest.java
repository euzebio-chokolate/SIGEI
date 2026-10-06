package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.service.armazenamento.CloudinaryService;
import ac.gov.br.sigei_backend.dto.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.InfraestruturaEscolaMapper;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.InfraestruturaEscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InfraestruturaEscolaServiceTest {

 @Mock
 CloudinaryService cloudinaryService;

 @Mock
 RegistroAnualRepository registroAnualRepository;

 @Mock
 InfraestruturaEscolaRepository infraestruturaRepository;

 @Mock
 InfraestruturaEscolaMapper infraestruturaMapper;

 @InjectMocks
 InfraestruturaEscolaService infraestruturaEscolaService;

 @Test
 public void deveSalvarInfraestruturaEscolaComSucesso() {
  InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
          null, 1L, 2020, 5, -8.5, -72.0, "FUNCIONANDO", "FLUVIAL",
          true, "CACIMBA", true, "SOLAR", true
  );

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setId(1L);

  InfraestruturaEscola infraConvertida = new InfraestruturaEscola();
  InfraestruturaEscola infraSalva = new InfraestruturaEscola();
  InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

  when(registroAnualRepository.findById(1L)).thenReturn(Optional.of(registroAnual));
  when(infraestruturaMapper.converter(requestDTO)).thenReturn(infraConvertida);
  when(infraestruturaRepository.save(any(InfraestruturaEscola.class))).thenReturn(infraSalva);
  lenient().when(infraestruturaMapper.converter(infraSalva)).thenReturn(responseMock);

  InfraestruturaEscolaResponseDTO resultado = infraestruturaEscolaService.salvar(requestDTO);

  assertNotNull(resultado);
  verify(registroAnualRepository, times(1)).findById(1L);
  verify(infraestruturaRepository, times(1)).save(infraConvertida);
 }

 @Test
 public void deveRecusarTipoDeAcessoForaDasOpcoes() {
  InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
          null, 1L, 2020, 5, -8.5, -72.0, "FUNCIONANDO", "AEREO",
          true, "CACIMBA", true, "SOLAR", true
  );
  InfraestruturaEscola infraConvertida = new InfraestruturaEscola();
  infraConvertida.setSituacao("FUNCIONANDO");
  infraConvertida.setTipoAcesso("AEREO");

  when(registroAnualRepository.findById(1L)).thenReturn(Optional.of(new RegistroAnual()));
  when(infraestruturaMapper.converter(requestDTO)).thenReturn(infraConvertida);

  IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
          () -> infraestruturaEscolaService.salvar(requestDTO));
  assertEquals("Tipo de acesso inválido: use TERRESTRE, FLUVIAL, MISTO.", erro.getMessage());
  verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
 }

 @Test
 public void deveLancarExcecaoAoSalvarComRegistroAnualInvalido() {
  InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
          null, 99L, 2020, 5, -8.5, -72.0, "FUNCIONANDO", "FLUVIAL",
          true, "CACIMBA", true, "SOLAR", true
  );

  when(registroAnualRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaEscolaService.salvar(requestDTO));

  assertEquals("Registro anual não encontrado", erro.getMessage());
  verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
 }

 @Test
 public void deveBuscarTodasAsInfraestruturas() {
  List<InfraestruturaEscola> lista = new ArrayList<>();
  lista.add(new InfraestruturaEscola());
  lista.add(new InfraestruturaEscola());

  InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

  when(infraestruturaRepository.findAll()).thenReturn(lista);
  lenient().when(infraestruturaMapper.converter(any(InfraestruturaEscola.class))).thenReturn(responseMock);

  List<InfraestruturaEscolaResponseDTO> resultado = infraestruturaEscolaService.buscarTudo();

  assertNotNull(resultado);
  assertEquals(2, resultado.size());
  verify(infraestruturaRepository, times(1)).findAll();
  verify(infraestruturaMapper, times(2)).converter(any(InfraestruturaEscola.class));
 }

 @Test
 public void deveBuscarInfraestruturasPorAno() {
  List<InfraestruturaEscola> lista = new ArrayList<>();
  lista.add(new InfraestruturaEscola());

  InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

  when(infraestruturaRepository.findByRegistroAnual_AnoReferencia_Ano(2026)).thenReturn(lista);
  lenient().when(infraestruturaMapper.converter(any(InfraestruturaEscola.class))).thenReturn(responseMock);

  List<InfraestruturaEscolaResponseDTO> resultado = infraestruturaEscolaService.buscarPorAno(2026);

  assertNotNull(resultado);
  assertEquals(1, resultado.size());
  verify(infraestruturaRepository, times(1)).findByRegistroAnual_AnoReferencia_Ano(2026);
 }

 @Test
 public void deveEditarInfraestruturaEscolaComSucesso() {
  InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
          1L, 2L, 2020, 5, -8.5, -72.0, "FUNCIONANDO", "FLUVIAL",
          true, "CACIMBA", true, "SOLAR", true
  );

  InfraestruturaEscola infraExistente = new InfraestruturaEscola();
  infraExistente.setId(1L);

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setId(2L);

  InfraestruturaEscola infraSalva = new InfraestruturaEscola();
  InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

  when(infraestruturaRepository.findById(1L)).thenReturn(Optional.of(infraExistente));
  when(registroAnualRepository.findById(2L)).thenReturn(Optional.of(registroAnual));
  doNothing().when(infraestruturaMapper).atualizar(requestDTO, infraExistente);
  when(infraestruturaRepository.save(any(InfraestruturaEscola.class))).thenReturn(infraSalva);
  lenient().when(infraestruturaMapper.converter(infraSalva)).thenReturn(responseMock);

  InfraestruturaEscolaResponseDTO resultado = infraestruturaEscolaService.editar(requestDTO);

  assertNotNull(resultado);
  verify(infraestruturaRepository, times(1)).findById(1L);
  verify(registroAnualRepository, times(1)).findById(2L);
  verify(infraestruturaRepository, times(1)).save(infraExistente);
 }

 @Test
 public void deveLancarExcecaoAoEditarInfraestruturaInexistente() {
  InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
          99L, 1L, 2020, 5, -8.5, -72.0, "FUNCIONANDO", "FLUVIAL",
          true, "CACIMBA", true, "SOLAR", true
  );

  when(infraestruturaRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaEscolaService.editar(requestDTO));

  assertEquals("Infraestrutura não encontrada", erro.getMessage());
  verify(registroAnualRepository, never()).findById(anyLong());
  verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
 }

 @Test
 public void deveLancarExcecaoAoEditarComRegistroAnualInexistente() {
  InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
          1L, 99L, 2020, 5, -8.5, -72.0, "FUNCIONANDO", "FLUVIAL",
          true, "CACIMBA", true, "SOLAR", true
  );

  InfraestruturaEscola infraExistente = new InfraestruturaEscola();
  infraExistente.setId(1L);

  when(infraestruturaRepository.findById(1L)).thenReturn(Optional.of(infraExistente));
  when(registroAnualRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaEscolaService.editar(requestDTO));

  assertEquals("Registro anual não encontrado", erro.getMessage());
  verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
 }

 @Test
 public void deveAdicionarFotoComSucesso() throws IOException {
  Long id = 1L;
  InfraestruturaEscola infra = new InfraestruturaEscola();
  infra.setId(id);

  MultipartFile fotoMock = mock(MultipartFile.class);
  Map<String, Object> respostaCloudinary = new HashMap<>();
  respostaCloudinary.put("secure_url", "https://cloudinary.com/foto.jpg");
  respostaCloudinary.put("public_id", "public_123");

  InfraestruturaEscola infraSalva = new InfraestruturaEscola();
  InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

  when(infraestruturaRepository.findById(id)).thenReturn(Optional.of(infra));
  when(cloudinaryService.uploadImagem(fotoMock)).thenReturn(respostaCloudinary);
  when(infraestruturaRepository.save(any(InfraestruturaEscola.class))).thenReturn(infraSalva);
  lenient().when(infraestruturaMapper.converter(infraSalva)).thenReturn(responseMock);

  InfraestruturaEscolaResponseDTO resultado = infraestruturaEscolaService.adicionarFoto(id, fotoMock);

  assertNotNull(resultado);
  verify(infraestruturaRepository, times(1)).findById(id);
  verify(cloudinaryService, times(1)).uploadImagem(fotoMock);
  verify(infraestruturaRepository, times(1)).save(infra);
 }

 @Test
 public void deveLancarExcecaoAoAdicionarFotoComInfraestruturaInexistente() throws IOException {
  Long id = 99L;
  MultipartFile fotoMock = mock(MultipartFile.class);

  when(infraestruturaRepository.findById(id)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaEscolaService.adicionarFoto(id, fotoMock));

  assertEquals("Infraestrutura não encontrada", erro.getMessage());
  verify(cloudinaryService, never()).uploadImagem(any());
  verify(infraestruturaRepository, never()).save(any());
 }

 @Test
 public void deveExcluirInfraestruturaComSucesso() {
  Long id = 1L;
  InfraestruturaEscola infra = new InfraestruturaEscola();
  infra.setId(id);

  when(infraestruturaRepository.findById(id)).thenReturn(Optional.of(infra));
  doNothing().when(infraestruturaRepository).deleteById(id);

  assertDoesNotThrow(() -> infraestruturaEscolaService.excluir(id));

  verify(infraestruturaRepository, times(1)).findById(id);
  verify(infraestruturaRepository, times(1)).deleteById(id);
 }

 @Test
 public void deveLancarExcecaoAoExcluirInfraestruturaInexistente() {
  Long id = 99L;

  when(infraestruturaRepository.findById(id)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaEscolaService.excluir(id));

  assertEquals("Infraestrutura não encontrada", erro.getMessage());
  verify(infraestruturaRepository, never()).deleteById(anyLong());
 }
}