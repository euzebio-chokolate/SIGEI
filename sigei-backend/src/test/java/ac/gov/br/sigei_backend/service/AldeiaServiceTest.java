package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.mappers.AldeiaMapper;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
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
public class AldeiaServiceTest {

    @Mock
    AldeiaRepository aldeiaRepository;

    @Mock
    TerraIndigenaRepository terraIndigenaRepository;

    @Mock
    PovoRepository povoRepository;

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    AldeiaMapper aldeiaMapper;

    @InjectMocks
    AldeiaService aldeiaService;

    @Test
    public void deveSalvarAldeiaComSucesso() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(null, "Aldeia Nova", 1L, 2L);

        TerraIndigena terra = new TerraIndigena();
        terra.setId(1L);

        Povo povo = new Povo();
        povo.setId(2L);

        Aldeia aldeiaConvertida = new Aldeia();
        aldeiaConvertida.setNome("Aldeia Nova");

        Aldeia aldeiaSalva = new Aldeia();
        aldeiaSalva.setId(1L);
        aldeiaSalva.setNome("Aldeia Nova");

        AldeiaResponseDTO responseMock = mock(AldeiaResponseDTO.class);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terra));
        when(povoRepository.findById(2L)).thenReturn(Optional.of(povo));
        when(aldeiaMapper.converter(requestDTO)).thenReturn(aldeiaConvertida);
        when(aldeiaRepository.save(any(Aldeia.class))).thenReturn(aldeiaSalva);
        lenient().when(aldeiaMapper.converter(aldeiaSalva)).thenReturn(responseMock);

        AldeiaResponseDTO resultado = aldeiaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(terraIndigenaRepository, times(1)).findById(1L);
        verify(povoRepository, times(1)).findById(2L);
        verify(aldeiaRepository, times(1)).save(aldeiaConvertida);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComTerraIndigenaInvalida() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(null, "Aldeia Nova", 99L, 2L);
        when(terraIndigenaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> aldeiaService.salvar(requestDTO));

        assertEquals("Terra indígena não encontrada", erro.getMessage());
        verify(povoRepository, never()).findById(anyLong());
        verify(aldeiaRepository, never()).save(any(Aldeia.class));
    }

    @Test
    public void deveLancarExcecaoAoSalvarComPovoInvalido() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(null, "Aldeia Nova", 1L, 99L);

        TerraIndigena terra = new TerraIndigena();
        terra.setId(1L);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terra));
        when(povoRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> aldeiaService.salvar(requestDTO));

        assertEquals("Povo não encontrado", erro.getMessage());
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
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(1L, "Aldeia Editada", 2L, 3L);

        Aldeia aldeiaExistente = new Aldeia();
        aldeiaExistente.setId(1L);

        TerraIndigena terraNova = new TerraIndigena();
        terraNova.setId(2L);

        Povo povoNovo = new Povo();
        povoNovo.setId(3L);

        Aldeia aldeiaConvertida = new Aldeia();
        AldeiaResponseDTO responseMock = mock(AldeiaResponseDTO.class);

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeiaExistente));
        when(terraIndigenaRepository.findById(2L)).thenReturn(Optional.of(terraNova));
        when(povoRepository.findById(3L)).thenReturn(Optional.of(povoNovo));

        when(aldeiaMapper.converter(requestDTO)).thenReturn(aldeiaConvertida);
        when(aldeiaRepository.save(any(Aldeia.class))).thenReturn(aldeiaConvertida);

        lenient().when(aldeiaMapper.converter(any(Aldeia.class))).thenReturn(responseMock);

        AldeiaResponseDTO resultado = aldeiaService.editar(requestDTO.id(), requestDTO);

        assertNotNull(resultado);
        verify(aldeiaRepository, times(1)).findById(1L);
        verify(terraIndigenaRepository, times(1)).findById(2L);
        verify(povoRepository, times(1)).findById(3L);
        verify(aldeiaRepository, times(1)).save(aldeiaConvertida);
    }

    @Test
    public void deveLancarExcecaoAoEditarAldeiaInexistente() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(99L, "Aldeia Editada", 1L, 2L);
        when(aldeiaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> aldeiaService.editar(requestDTO.id(), requestDTO));

        assertEquals("Aldeia não encontrada", erro.getMessage());
        verify(terraIndigenaRepository, never()).findById(anyLong());
        verify(povoRepository, never()).findById(anyLong());
        verify(aldeiaRepository, never()).save(any(Aldeia.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComTerraIndigenaInexistente() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(1L, "Aldeia Editada", 99L, 2L);

        Aldeia aldeiaExistente = new Aldeia();
        aldeiaExistente.setId(1L);

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeiaExistente));
        when(terraIndigenaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> aldeiaService.editar(requestDTO.id(), requestDTO));

        assertEquals("Terra indígena não encontrada", erro.getMessage());
        verify(povoRepository, never()).findById(anyLong());
        verify(aldeiaRepository, never()).save(any(Aldeia.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComPovoInexistente() {
        AldeiaRequestDTO requestDTO = new AldeiaRequestDTO(1L, "Aldeia Editada", 2L, 99L);

        Aldeia aldeiaExistente = new Aldeia();
        aldeiaExistente.setId(1L);

        TerraIndigena terraNova = new TerraIndigena();
        terraNova.setId(2L);

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeiaExistente));
        when(terraIndigenaRepository.findById(2L)).thenReturn(Optional.of(terraNova));
        when(povoRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> aldeiaService.editar(requestDTO.id(), requestDTO));

        assertEquals("Povo não encontrado", erro.getMessage());
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

    @Test
    public void deveLancarExcecaoAoExcluirAldeiaInexistente() {
        Long id = 99L;
        when(aldeiaRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> aldeiaService.excluir(id));

        assertEquals("Id de aldeia inválido ou não encontrado", erro.getMessage());
        verify(aldeiaRepository, never()).deleteById(anyLong());
    }
}