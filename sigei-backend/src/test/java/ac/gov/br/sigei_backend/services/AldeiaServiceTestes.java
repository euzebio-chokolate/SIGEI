package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.AldeiaMapper;
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
public class AldeiaServiceTestes {

    @Mock
    AldeiaRepository aldeiaRepository;

    @Mock
    TerraIndigenaRepository terraIndigenaRepository;

    @Mock
    AldeiaMapper aldeiaMapper;

    @InjectMocks
    AldeiaService aldeiaService;

    @Test
    public void deveSalvarAldeiaComSucesso() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(null, "Aldeia Nova", 1L);
        TerraIndigena terra = new TerraIndigena();
        terra.setId(1L);

        Aldeia aldeiaSalva = new Aldeia();
        aldeiaSalva.setId(1L);
        aldeiaSalva.setNome("Aldeia Nova");
        aldeiaSalva.setTerraIndigena(terra);

        AldeiaResponseDTO responseMock = mock(AldeiaResponseDTO.class);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terra));
        when(aldeiaRepository.save(any(Aldeia.class))).thenReturn(aldeiaSalva);
        lenient().when(aldeiaMapper.converter(any(Aldeia.class))).thenReturn(responseMock);

        AldeiaResponseDTO resultado = aldeiaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(terraIndigenaRepository, times(1)).findById(1L);
        verify(aldeiaRepository, times(1)).save(any(Aldeia.class));
    }

    @Test
    public void deveLancarExcecaoAoSalvarComTerraIndigenaInvalida() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(null, "Aldeia Nova", 99L);
        when(terraIndigenaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            aldeiaService.salvar(requestDTO);
        });

        assertEquals("Terra indígena não encontrada", erro.getMessage());
        verify(aldeiaRepository, never()).save(any(Aldeia.class));
    }

    @Test
    public void deveBuscarTodasAsAldeias() {
        List<Aldeia> lista = new ArrayList<>();
        lista.add(new Aldeia());
        lista.add(new Aldeia());

        AldeiaResponseDTO responseMock = mock(AldeiaResponseDTO.class);

        when(aldeiaRepository.findAll()).thenReturn(lista);
        lenient().when(aldeiaMapper.converter(any(Aldeia.class))).thenReturn(responseMock);

        List<AldeiaResponseDTO> resultado = aldeiaService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(aldeiaRepository, times(1)).findAll();
        verify(aldeiaMapper, times(2)).converter(any(Aldeia.class));
    }

    @Test
    public void deveEditarAldeiaComSucesso() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(1L, "Aldeia Editada", 2L);

        Aldeia aldeiaAntiga = new Aldeia();
        aldeiaAntiga.setId(1L);
        aldeiaAntiga.setNome("Nome Antigo");

        TerraIndigena terraNova = new TerraIndigena();
        terraNova.setId(2L);

        AldeiaResponseDTO responseMock = mock(AldeiaResponseDTO.class);

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeiaAntiga));
        when(terraIndigenaRepository.findById(2L)).thenReturn(Optional.of(terraNova));
        when(aldeiaRepository.save(any(Aldeia.class))).thenReturn(aldeiaAntiga);
        lenient().when(aldeiaMapper.converter(any(Aldeia.class))).thenReturn(responseMock);

        AldeiaResponseDTO resultado = aldeiaService.editar(requestDTO);

        assertNotNull(resultado);
        verify(aldeiaRepository, times(1)).findById(1L);
        verify(terraIndigenaRepository, times(1)).findById(2L);
        verify(aldeiaRepository, times(2)).save(any(Aldeia.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarAldeiaInexistente() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(99L, "Aldeia Editada", 1L);
        when(aldeiaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            aldeiaService.editar(requestDTO);
        });

        assertEquals("Id de aldeia inválido ou não encontrado ", erro.getMessage());
        verify(terraIndigenaRepository, never()).findById(anyLong());
        verify(aldeiaRepository, never()).save(any(Aldeia.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComTerraIndigenaInexistente() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(1L, "Aldeia Editada", 99L);

        Aldeia aldeiaExistente = new Aldeia();
        aldeiaExistente.setId(1L);

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeiaExistente));
        when(terraIndigenaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            aldeiaService.editar(requestDTO);
        });

        assertEquals("Id de terra indígena inválido ou não encontrado", erro.getMessage());
        verify(aldeiaRepository, never()).save(any(Aldeia.class));
    }

    @Test
    public void deveExcluirAldeiaComSucesso() {
        Long id = 1L;
        Aldeia aldeia = new Aldeia();
        aldeia.setId(id);

        when(aldeiaRepository.findById(id)).thenReturn(Optional.of(aldeia));
        doNothing().when(aldeiaRepository).deleteById(id);

        assertDoesNotThrow(() -> aldeiaService.excluir(id));

        verify(aldeiaRepository, times(1)).findById(id);
        verify(aldeiaRepository, times(1)).deleteById(id);
    }
}