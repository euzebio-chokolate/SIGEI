package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.MatriculaNivelRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MatriculaNivelResponseDTO;
import ac.gov.br.sigei_backend.mappers.MatriculaNivelMapper;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.MatriculaNivelRepository;
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
public class MatriculaNivelServiceTest {

    @Mock
    RegistroAnualRepository registroRepository;

    @Mock
    MatriculaNivelRepository matriculaRepository;

    @Mock
    MatriculaNivelMapper matriculaMapper;

    @InjectMocks
    MatriculaNivelService matriculaService;

    @Test
    public void deveSalvarMatriculaNivelComSucesso() {
        MatriculaNivelRequestDTO requestDTO = new MatriculaNivelRequestDTO(null, 1L, "Ensino Fundamental I", 15, 20, 35);
        RegistroAnual registro = new RegistroAnual();
        registro.setId(1L);

        MatriculaNivel matriculaConvertida = new MatriculaNivel();
        MatriculaNivel matriculaSalva = new MatriculaNivel();
        MatriculaNivelResponseDTO responseMock = mock(MatriculaNivelResponseDTO.class);

        when(registroRepository.findById(1L)).thenReturn(Optional.of(registro));
        when(matriculaMapper.converter(requestDTO)).thenReturn(matriculaConvertida);
        when(matriculaRepository.save(any(MatriculaNivel.class))).thenReturn(matriculaSalva);
        lenient().when(matriculaMapper.converter(matriculaSalva)).thenReturn(responseMock);

        MatriculaNivelResponseDTO resultado = matriculaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(registroRepository, times(1)).findById(1L);
        verify(matriculaRepository, times(1)).save(matriculaConvertida);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComRegistroInvalido() {
        MatriculaNivelRequestDTO requestDTO = new MatriculaNivelRequestDTO(null, 99L, "Ensino Fundamental I", 15, 20, 35);
        when(registroRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaService.salvar(requestDTO));

        assertEquals("Registro anual não encontrado", erro.getMessage());
        verify(matriculaRepository, never()).save(any(MatriculaNivel.class));
    }

    @Test
    public void deveBuscarTodasAsMatriculas() {
        List<MatriculaNivel> lista = new ArrayList<>();
        lista.add(new MatriculaNivel());
        lista.add(new MatriculaNivel());

        MatriculaNivelResponseDTO responseMock = mock(MatriculaNivelResponseDTO.class);

        when(matriculaRepository.findAll()).thenReturn(lista);
        lenient().when(matriculaMapper.converter(any(MatriculaNivel.class))).thenReturn(responseMock);

        List<MatriculaNivelResponseDTO> resultado = matriculaService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(matriculaRepository, times(1)).findAll();
        verify(matriculaMapper, times(2)).converter(any(MatriculaNivel.class));
    }

    @Test
    public void deveEditarMatriculaNivelComSucesso() {
        MatriculaNivelRequestDTO requestDTO = new MatriculaNivelRequestDTO(1L, 2L, "Ensino Fundamental II", 10, 15, 25);
        MatriculaNivel matriculaExistente = new MatriculaNivel();
        matriculaExistente.setId(1L);

        RegistroAnual registroNovo = new RegistroAnual();
        registroNovo.setId(2L);

        MatriculaNivel matriculaSalva = new MatriculaNivel();
        MatriculaNivelResponseDTO responseMock = mock(MatriculaNivelResponseDTO.class);

        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matriculaExistente));
        when(registroRepository.findById(2L)).thenReturn(Optional.of(registroNovo));
        doNothing().when(matriculaMapper).atualizar(requestDTO, matriculaExistente);
        when(matriculaRepository.save(any(MatriculaNivel.class))).thenReturn(matriculaSalva);
        lenient().when(matriculaMapper.converter(matriculaSalva)).thenReturn(responseMock);

        MatriculaNivelResponseDTO resultado = matriculaService.editar(requestDTO);

        assertNotNull(resultado);
        verify(matriculaRepository, times(1)).findById(1L);
        verify(registroRepository, times(1)).findById(2L);
        verify(matriculaRepository, times(1)).save(matriculaExistente);
    }

    @Test
    public void deveLancarExcecaoAoEditarMatriculaInexistente() {
        MatriculaNivelRequestDTO requestDTO = new MatriculaNivelRequestDTO(99L, 1L, "Ensino Fundamental I", 10, 10, 20);
        when(matriculaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaService.editar(requestDTO));

        assertEquals("Matrícula não encontrada", erro.getMessage());
        verify(registroRepository, never()).findById(anyLong());
        verify(matriculaRepository, never()).save(any(MatriculaNivel.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComRegistroInexistente() {
        MatriculaNivelRequestDTO requestDTO = new MatriculaNivelRequestDTO(1L, 99L, "Ensino Fundamental I", 10, 10, 20);
        MatriculaNivel matriculaExistente = new MatriculaNivel();
        matriculaExistente.setId(1L);

        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matriculaExistente));
        when(registroRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaService.editar(requestDTO));

        assertEquals("Registro anual não encontrado", erro.getMessage());
        verify(matriculaRepository, never()).save(any(MatriculaNivel.class));
    }

    @Test
    public void deveExcluirMatriculaNivelComSucesso() {
        Long id = 1L;
        MatriculaNivel matricula = new MatriculaNivel();
        matricula.setId(id);

        when(matriculaRepository.findById(id)).thenReturn(Optional.of(matricula));
        doNothing().when(matriculaRepository).deleteById(id);

        assertDoesNotThrow(() -> matriculaService.excluir(id));

        verify(matriculaRepository, times(1)).findById(id);
        verify(matriculaRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirMatriculaInexistente() {
        Long id = 99L;
        when(matriculaRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> matriculaService.excluir(id));

        assertEquals("Matrícula não encontrada", erro.getMessage());
        verify(matriculaRepository, never()).deleteById(anyLong());
    }
}