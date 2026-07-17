package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.AnexoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AnexoResponseDTO;
import ac.gov.br.sigei_backend.mappers.AnexoMapper;
import ac.gov.br.sigei_backend.models.Anexo;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.repository.AnexoRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
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
public class AnexoServiceTestes {

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    AnexoRepository anexoRepository;

    @Mock
    AnexoMapper anexoMapper;

    @InjectMocks
    AnexoService anexoService;

    @Test
    public void deveSalvarAnexoComSucesso() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(null, 1L, 2L);

        Escola escola = new Escola();
        escola.setId(1L);

        Escola escolaAnexo = new Escola();
        escolaAnexo.setId(2L);

        Anexo anexoConvertido = new Anexo();
        Anexo anexoSalvo = new Anexo();
        AnexoResponseDTO responseMock = mock(AnexoResponseDTO.class);

        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escolaAnexo));
        when(anexoMapper.converter(requestDTO)).thenReturn(anexoConvertido);
        when(anexoRepository.save(any(Anexo.class))).thenReturn(anexoSalvo);
        lenient().when(anexoMapper.converter(anexoSalvo)).thenReturn(responseMock);

        AnexoResponseDTO resultado = anexoService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(escolaRepository, times(1)).findById(1L);
        verify(escolaRepository, times(1)).findById(2L);
        verify(anexoRepository, times(1)).save(anexoConvertido);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComEscolaInvalida() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(null, 99L, 2L);
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> anexoService.salvar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(escolaRepository, never()).findById(2L);
        verify(anexoRepository, never()).save(any(Anexo.class));
    }

    @Test
    public void deveLancarExcecaoAoSalvarComEscolaAnexoInvalida() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(null, 1L, 99L);
        Escola escola = new Escola();
        escola.setId(1L);

        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> anexoService.salvar(requestDTO));

        assertEquals("Escola anexo não encontrada", erro.getMessage());
        verify(anexoRepository, never()).save(any(Anexo.class));
    }

    @Test
    public void deveBuscarTodosOsAnexos() {
        List<Anexo> lista = new ArrayList<>();
        lista.add(new Anexo());
        lista.add(new Anexo());

        AnexoResponseDTO responseMock = mock(AnexoResponseDTO.class);

        when(anexoRepository.findAll()).thenReturn(lista);
        lenient().when(anexoMapper.converter(any(Anexo.class))).thenReturn(responseMock);

        List<AnexoResponseDTO> resultado = anexoService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(anexoRepository, times(1)).findAll();
        verify(anexoMapper, times(2)).converter(any(Anexo.class));
    }

    @Test
    public void deveEditarAnexoComSucesso() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(1L, 2L, 3L);

        Anexo anexoExistente = new Anexo();
        anexoExistente.setId(1L);

        Escola escola = new Escola();
        escola.setId(2L);

        Escola escolaAnexo = new Escola();
        escolaAnexo.setId(3L);

        Anexo anexoSalvo = new Anexo();
        AnexoResponseDTO responseMock = mock(AnexoResponseDTO.class);

        when(anexoRepository.findById(1L)).thenReturn(Optional.of(anexoExistente));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escola));
        when(escolaRepository.findById(3L)).thenReturn(Optional.of(escolaAnexo));
        doNothing().when(anexoMapper).atualizar(requestDTO, anexoExistente);
        when(anexoRepository.save(any(Anexo.class))).thenReturn(anexoSalvo);
        lenient().when(anexoMapper.converter(anexoSalvo)).thenReturn(responseMock);

        AnexoResponseDTO resultado = anexoService.editar(requestDTO);

        assertNotNull(resultado);
        verify(anexoRepository, times(1)).findById(1L);
        verify(escolaRepository, times(1)).findById(2L);
        verify(escolaRepository, times(1)).findById(3L);
        verify(anexoRepository, times(1)).save(anexoExistente);
    }

    @Test
    public void deveLancarExcecaoAoEditarAnexoInexistente() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(99L, 1L, 2L);
        when(anexoRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> anexoService.editar(requestDTO));

        assertEquals("Anexo não encontrado", erro.getMessage());
        verify(escolaRepository, never()).findById(anyLong());
        verify(anexoRepository, never()).save(any(Anexo.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComEscolaInexistente() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(1L, 99L, 2L);
        Anexo anexoExistente = new Anexo();
        anexoExistente.setId(1L);

        when(anexoRepository.findById(1L)).thenReturn(Optional.of(anexoExistente));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> anexoService.editar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(anexoRepository, never()).save(any(Anexo.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComEscolaAnexoInexistente() {
        AnexoRequestDTO requestDTO = new AnexoRequestDTO(1L, 2L, 99L);
        Anexo anexoExistente = new Anexo();
        anexoExistente.setId(1L);

        Escola escola = new Escola();
        escola.setId(2L);

        when(anexoRepository.findById(1L)).thenReturn(Optional.of(anexoExistente));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escola));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> anexoService.editar(requestDTO));

        assertEquals("Escola anexo não encontrada", erro.getMessage());
        verify(anexoRepository, never()).save(any(Anexo.class));
    }

    @Test
    public void deveExcluirAnexoComSucesso() {
        Long id = 1L;
        Anexo anexo = new Anexo();
        anexo.setId(id);

        when(anexoRepository.findById(id)).thenReturn(Optional.of(anexo));
        doNothing().when(anexoRepository).deleteById(id);

        assertDoesNotThrow(() -> anexoService.excluir(id));

        verify(anexoRepository, times(1)).findById(id);
        verify(anexoRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirAnexoInexistente() {
        Long id = 99L;
        when(anexoRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> anexoService.excluir(id));

        assertEquals("Anexo não encontrado", erro.getMessage());
        verify(anexoRepository, never()).deleteById(anyLong());
    }
}