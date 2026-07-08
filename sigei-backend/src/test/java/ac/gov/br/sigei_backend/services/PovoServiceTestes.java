package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.mappers.PovoMapper;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.repository.PovoRepository;
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
public class PovoServiceTestes {

    @Mock
    PovoRepository povoRepository;

    @Mock
    PovoMapper povoMapper;

    @InjectMocks
    PovoService povoService;

    @Test
    public void deveSalvarPovoComSucesso() {
        PovoRequestDTO requestDTO = new PovoRequestDTO(null, "Huni Kuin");
        Povo povoConvertido = new Povo();
        Povo povoSalvo = new Povo();
        PovoResponseDTO responseMock = mock(PovoResponseDTO.class);

        when(povoMapper.converter(requestDTO)).thenReturn(povoConvertido);
        when(povoRepository.save(any(Povo.class))).thenReturn(povoSalvo);
        lenient().when(povoMapper.converter(povoSalvo)).thenReturn(responseMock);

        PovoResponseDTO resultado = povoService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(povoRepository, times(1)).save(povoConvertido);
    }

    @Test
    public void deveBuscarTodosOsPovos() {
        List<Povo> lista = new ArrayList<>();
        lista.add(new Povo());
        lista.add(new Povo());

        PovoResponseDTO responseMock = mock(PovoResponseDTO.class);

        when(povoRepository.findAll()).thenReturn(lista);
        lenient().when(povoMapper.converter(any(Povo.class))).thenReturn(responseMock);

        List<PovoResponseDTO> resultado = povoService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(povoRepository, times(1)).findAll();
        verify(povoMapper, times(2)).converter(any(Povo.class));
    }

    @Test
    public void deveEditarPovoComSucesso() {
        PovoRequestDTO requestDTO = new PovoRequestDTO(1L, "Huni Kuin Editado");
        Povo povoExistente = new Povo();
        povoExistente.setId(1L);
        Povo povoConvertido = new Povo();
        Povo povoSalvo = new Povo();
        PovoResponseDTO responseMock = mock(PovoResponseDTO.class);

        when(povoRepository.findById(1L)).thenReturn(Optional.of(povoExistente));
        when(povoMapper.converter(requestDTO)).thenReturn(povoConvertido);
        when(povoRepository.save(any(Povo.class))).thenReturn(povoSalvo);
        lenient().when(povoMapper.converter(povoSalvo)).thenReturn(responseMock);

        PovoResponseDTO resultado = povoService.editar(requestDTO);

        assertNotNull(resultado);
        verify(povoRepository, times(1)).findById(1L);
        verify(povoRepository, times(1)).save(povoConvertido);
    }

    @Test
    public void deveLancarExcecaoAoEditarPovoInexistente() {
        PovoRequestDTO requestDTO = new PovoRequestDTO(99L, "Huni Kuin");
        when(povoRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> povoService.editar(requestDTO));

        assertEquals("Povo não encontrado", erro.getMessage());
        verify(povoRepository, never()).save(any(Povo.class));
    }

    @Test
    public void deveExcluirPovoComSucesso() {
        Long id = 1L;
        Povo povo = new Povo();
        povo.setId(id);

        when(povoRepository.findById(id)).thenReturn(Optional.of(povo));
        doNothing().when(povoRepository).deleteById(id);

        assertDoesNotThrow(() -> povoService.excluir(id));

        verify(povoRepository, times(1)).findById(id);
        verify(povoRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirPovoInexistente() {
        Long id = 99L;
        when(povoRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> povoService.excluir(id));

        assertEquals("Id de povo inválido ou não encontrado", erro.getMessage());
        verify(povoRepository, never()).deleteById(anyLong());
    }
}