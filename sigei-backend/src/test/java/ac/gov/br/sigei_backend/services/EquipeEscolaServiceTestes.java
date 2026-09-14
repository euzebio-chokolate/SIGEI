package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.EquipeEscolaMapper;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.EquipeEscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EquipeEscolaServiceTestes {

 @Mock
 RegistroAnualRepository registroAnualRepository;

 @Mock
 EquipeEscolaRepository equipeEscolaRepository;

 @Mock
 EquipeEscolaMapper equipeEscolaMapper;

 @InjectMocks
 EquipeEscolaService equipeEscolaService;

 @Test
 public void deveSalvarEquipeEscolaComSucesso() {
  EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(null, 1L, true, true, true, true, true, true);

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setId(1L);

  EquipeEscola equipeConvertida = new EquipeEscola();
  EquipeEscola equipeSalva = new EquipeEscola();
  EquipeEscolaResponseDTO responseMock = mock(EquipeEscolaResponseDTO.class);

  when(registroAnualRepository.findById(1L)).thenReturn(Optional.of(registroAnual));
  when(equipeEscolaMapper.converter(requestDTO)).thenReturn(equipeConvertida);
  when(equipeEscolaRepository.save(any(EquipeEscola.class))).thenReturn(equipeSalva);
  lenient().when(equipeEscolaMapper.converter(equipeSalva)).thenReturn(responseMock);

  EquipeEscolaResponseDTO resultado = equipeEscolaService.salvar(requestDTO);

  assertNotNull(resultado);
  verify(registroAnualRepository, times(1)).findById(1L);
  verify(equipeEscolaRepository, times(1)).save(equipeConvertida);
 }

 @Test
 public void deveLancarExcecaoAoSalvarComRegistroAnualInvalido() {
  EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(null, 99L, true, true, true, true, true, true);

  when(registroAnualRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.salvar(requestDTO));

  assertEquals("Registro anual não encontrado", erro.getMessage());
  verify(equipeEscolaRepository, never()).save(any(EquipeEscola.class));
 }

 @Test
 public void deveEditarEquipeEscolaComSucesso() {
  EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(1L, 2L, true, true, true, true, true, true);

  EquipeEscola equipeExistente = new EquipeEscola();
  equipeExistente.setId(1L);

  RegistroAnual registroAnual = new RegistroAnual();
  registroAnual.setId(2L);

  EquipeEscola equipeSalva = new EquipeEscola();
  EquipeEscolaResponseDTO responseMock = mock(EquipeEscolaResponseDTO.class);

  when(equipeEscolaRepository.findById(1L)).thenReturn(Optional.of(equipeExistente));
  when(registroAnualRepository.findById(2L)).thenReturn(Optional.of(registroAnual));
  doNothing().when(equipeEscolaMapper).atualizar(requestDTO, equipeExistente);
  when(equipeEscolaRepository.save(any(EquipeEscola.class))).thenReturn(equipeSalva);
  lenient().when(equipeEscolaMapper.converter(equipeSalva)).thenReturn(responseMock);

  EquipeEscolaResponseDTO resultado = equipeEscolaService.editar(requestDTO);

  assertNotNull(resultado);
  verify(equipeEscolaRepository, times(1)).findById(1L);
  verify(registroAnualRepository, times(1)).findById(2L);
  verify(equipeEscolaRepository, times(1)).save(equipeExistente);
 }

 @Test
 public void deveLancarExcecaoAoEditarEquipeEscolaInexistente() {
  EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(99L, 1L, true, true, true, true, true, true);

  when(equipeEscolaRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.editar(requestDTO));

  assertEquals("Equipe escolar não encontrada", erro.getMessage());
  verify(registroAnualRepository, never()).findById(anyLong());
  verify(equipeEscolaRepository, never()).save(any(EquipeEscola.class));
 }

 @Test
 public void deveLancarExcecaoAoEditarComRegistroAnualInexistente() {
  EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(1L, 99L, true, true, true, true, true, true);

  EquipeEscola equipeExistente = new EquipeEscola();
  equipeExistente.setId(1L);

  when(equipeEscolaRepository.findById(1L)).thenReturn(Optional.of(equipeExistente));
  when(registroAnualRepository.findById(99L)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.editar(requestDTO));

  assertEquals("Registro anual não encontrado", erro.getMessage());
  verify(equipeEscolaRepository, never()).save(any(EquipeEscola.class));
 }

 @Test
 public void deveExcluirEquipeEscolaComSucesso() {
  Long id = 1L;
  EquipeEscola equipe = new EquipeEscola();
  equipe.setId(id);

  when(equipeEscolaRepository.findById(id)).thenReturn(Optional.of(equipe));
  doNothing().when(equipeEscolaRepository).deleteById(id);

  assertDoesNotThrow(() -> equipeEscolaService.excluir(id));

  verify(equipeEscolaRepository, times(1)).findById(id);
  verify(equipeEscolaRepository, times(1)).deleteById(id);
 }

 @Test
 public void deveLancarExcecaoAoExcluirEquipeEscolaInexistente() {
  Long id = 99L;

  when(equipeEscolaRepository.findById(id)).thenReturn(Optional.empty());

  NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.excluir(id));

  assertEquals("Equipe escolar não encontrada", erro.getMessage());
  verify(equipeEscolaRepository, never()).deleteById(anyLong());
 }
}