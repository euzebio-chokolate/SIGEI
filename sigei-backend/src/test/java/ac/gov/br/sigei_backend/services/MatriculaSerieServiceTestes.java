package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MatriculaSerieRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MatriculaSerieResponseDTO;
import ac.gov.br.sigei_backend.mappers.MatriculaSerieMapper;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.MatriculaSerieRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatriculaSerieServiceTestes {

 @Mock
 RegistroAnualRepository registroRepository;

 @Mock
 MatriculaSerieRepository matriculaSerieRepository;

 @Mock
 MatriculaSerieMapper matriculaSerieMapper;

 @InjectMocks
 MatriculaSerieService matriculaSerieService;

 @Test
 public void deveSalvarMatriculaSerieComSucesso() {
  MatriculaSerieRequestDTO requestDTO = new MatriculaSerieRequestDTO(null, 1L, "Ensino Fundamental", "1º Ano", 15, 10, 25);

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setId(1L);

  MatriculaSerie matriculaConvertida = new MatriculaSerie();
  MatriculaSerie matriculaSalva = new MatriculaSerie();
  MatriculaSerieResponseDTO responseMock = mock(MatriculaSerieResponseDTO.class);

  when(registroRepository.findById(1L)).thenReturn(Optional.of(registroAnual));
  when(matriculaSerieMapper.converter(requestDTO)).thenReturn(matriculaConvertida);
  when(matriculaSerieRepository.save(any(MatriculaSerie.class))).thenReturn(matriculaSalva);
  lenient().when(matriculaSerieMapper.converter(matriculaSalva)).thenReturn(responseMock);

  MatriculaSerieResponseDTO resultado = matriculaSerieService.salvar(requestDTO);

  assertNotNull(resultado);
  verify(registroRepository, times(1)).findById(1L);
  verify(matriculaSerieRepository, times(1)).save(matriculaConvertida);
 }

 @Test
 public void deveLancarExcecaoAoSalvarComRegistroInvalido() {
  MatriculaSerieRequestDTO requestDTO = new MatriculaSerieRequestDTO(null, 99L, "Ensino Fundamental", "1º Ano", 15, 10, 25);

  when(registroRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaSerieService.salvar(requestDTO));

  assertEquals("Registro anual não encontrado", erro.getMessage());
  verify(matriculaSerieRepository, never()).save(any(MatriculaSerie.class));
 }

 @Test
 public void deveBuscarTodasAsMatriculasSerie() {
  List<MatriculaSerie> lista = new ArrayList<>();
  lista.add(new MatriculaSerie());
  lista.add(new MatriculaSerie());

  MatriculaSerieResponseDTO responseMock = mock(MatriculaSerieResponseDTO.class);

  when(matriculaSerieRepository.findAll()).thenReturn(lista);
  lenient().when(matriculaSerieMapper.converter(any(MatriculaSerie.class))).thenReturn(responseMock);

  List<MatriculaSerieResponseDTO> resultado = matriculaSerieService.buscarTudo();

  assertNotNull(resultado);
  assertEquals(2, resultado.size());
  verify(matriculaSerieRepository, times(1)).findAll();
  verify(matriculaSerieMapper, times(2)).converter(any(MatriculaSerie.class));
 }

 @Test
 public void deveEditarMatriculaSerieComSucesso() {
  MatriculaSerieRequestDTO requestDTO = new MatriculaSerieRequestDTO(1L, 2L, "Ensino Fundamental", "2º Ano", 18, 12, 30);

  MatriculaSerie matriculaExistente = new MatriculaSerie();
  matriculaExistente.setId(1L);

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setId(2L);

  MatriculaSerie matriculaSalva = new MatriculaSerie();
  MatriculaSerieResponseDTO responseMock = mock(MatriculaSerieResponseDTO.class);

  when(matriculaSerieRepository.findById(1L)).thenReturn(Optional.of(matriculaExistente));
  when(registroRepository.findById(2L)).thenReturn(Optional.of(registroAnual));
  doNothing().when(matriculaSerieMapper).atualizar(requestDTO, matriculaExistente);
  when(matriculaSerieRepository.save(any(MatriculaSerie.class))).thenReturn(matriculaSalva);
  lenient().when(matriculaSerieMapper.converter(matriculaSalva)).thenReturn(responseMock);

  MatriculaSerieResponseDTO resultado = matriculaSerieService.editar(requestDTO);

  assertNotNull(resultado);
  verify(matriculaSerieRepository, times(1)).findById(1L);
  verify(registroRepository, times(1)).findById(2L);
  verify(matriculaSerieRepository, times(1)).save(matriculaExistente);
 }

 @Test
 public void deveLancarExcecaoAoEditarMatriculaInexistente() {
  MatriculaSerieRequestDTO requestDTO = new MatriculaSerieRequestDTO(99L, 1L, "Ensino Fundamental", "1º Ano", 15, 10, 25);

  when(matriculaSerieRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaSerieService.editar(requestDTO));

  assertEquals("Matrícula não encontrada", erro.getMessage());
  verify(registroRepository, never()).findById(anyLong());
  verify(matriculaSerieRepository, never()).save(any(MatriculaSerie.class));
 }

 @Test
 public void deveLancarExcecaoAoEditarComRegistroInexistente() {
  MatriculaSerieRequestDTO requestDTO = new MatriculaSerieRequestDTO(1L, 99L, "Ensino Fundamental", "1º Ano", 15, 10, 25);

  MatriculaSerie matriculaExistente = new MatriculaSerie();
  matriculaExistente.setId(1L);

  when(matriculaSerieRepository.findById(1L)).thenReturn(Optional.of(matriculaExistente));
  when(registroRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaSerieService.editar(requestDTO));

  assertEquals("Registro anual não encontrado", erro.getMessage());
  verify(matriculaSerieRepository, never()).save(any(MatriculaSerie.class));
 }

 @Test
 public void deveExcluirMatriculaSerieComSucesso() {
  Long id = 1L;
  MatriculaSerie matricula = new MatriculaSerie();
  matricula.setId(id);

  when(matriculaSerieRepository.findById(id)).thenReturn(Optional.of(matricula));
  doNothing().when(matriculaSerieRepository).deleteById(id);

  assertDoesNotThrow(() -> matriculaSerieService.excluir(id));

  verify(matriculaSerieRepository, times(1)).findById(id);
  verify(matriculaSerieRepository, times(1)).deleteById(id);
 }

 @Test
 public void deveLancarExcecaoAoExcluirMatriculaInexistente() {
  Long id = 99L;

  when(matriculaSerieRepository.findById(id)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaSerieService.excluir(id));

  assertEquals("Matrícula não encontrada", erro.getMessage());
  verify(matriculaSerieRepository, never()).deleteById(anyLong());
 }
}