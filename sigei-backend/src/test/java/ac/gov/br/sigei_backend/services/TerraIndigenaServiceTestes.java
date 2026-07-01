package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.repository.TerraIndigenaRepository;
import ac.gov.br.sigei_backend.utils.TerraIndigenaMapper;
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
public class TerraIndigenaServiceTestes {

    @Mock
    TerraIndigenaRepository terraIndigenaRepository;

    @Mock
    MunicipioRepository municipioRepository;

    @Mock
    TerraIndigenaMapper terraIndigenaMapper;

    @InjectMocks
    TerraIndigenaService terraIndigenaService;

    @Test
    public void deveSalvarTerraIndigenaComSucesso() {
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(null, "Vale do Javari", 1L);
        Municipio municipio = new Municipio();
        municipio.setId(1L);

        TerraIndigena terraConvertida = new TerraIndigena();
        TerraIndigena terraSalva = new TerraIndigena();
        TerraIndigenaResponseDTO responseMock = mock(TerraIndigenaResponseDTO.class);

        when(municipioRepository.findById(1L)).thenReturn(Optional.of(municipio));
        when(terraIndigenaMapper.converter(requestDTO)).thenReturn(terraConvertida);
        when(terraIndigenaRepository.save(any(TerraIndigena.class))).thenReturn(terraSalva);
        lenient().when(terraIndigenaMapper.converter(terraSalva)).thenReturn(responseMock);

        TerraIndigenaResponseDTO resultado = terraIndigenaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(municipioRepository, times(1)).findById(1L);
        verify(terraIndigenaRepository, times(1)).save(terraConvertida);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComMunicipioInvalido() {
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(null, "Vale do Javari", 99L);
        when(municipioRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.salvar(requestDTO);
        });

        assertEquals("Municipio não encontrado", erro.getMessage());
        verify(terraIndigenaRepository, never()).save(any(TerraIndigena.class));
    }

    @Test
    public void deveBuscarTodasAsTerrasIndigenas() {
        List<TerraIndigena> lista = new ArrayList<>();
        lista.add(new TerraIndigena());
        lista.add(new TerraIndigena());

        TerraIndigenaResponseDTO responseMock = mock(TerraIndigenaResponseDTO.class);

        when(terraIndigenaRepository.findAll()).thenReturn(lista);
        lenient().when(terraIndigenaMapper.converter(any(TerraIndigena.class))).thenReturn(responseMock);

        List<TerraIndigenaResponseDTO> resultado = terraIndigenaService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(terraIndigenaRepository, times(1)).findAll();
        verify(terraIndigenaMapper, times(2)).converter(any(TerraIndigena.class));
    }

    @Test
    public void deveEditarTerraIndigenaComSucesso() {
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(1L, "Vale Editado", 2L);

        TerraIndigena terraExistente = new TerraIndigena();
        terraExistente.setId(1L);

        Municipio municipioNovo = new Municipio();
        municipioNovo.setId(2L);

        TerraIndigena terraConvertida = new TerraIndigena();
        TerraIndigenaResponseDTO responseMock = mock(TerraIndigenaResponseDTO.class);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terraExistente));
        when(municipioRepository.findById(2L)).thenReturn(Optional.of(municipioNovo));
        when(terraIndigenaMapper.converter(requestDTO)).thenReturn(terraConvertida);
        when(terraIndigenaRepository.save(any(TerraIndigena.class))).thenReturn(terraConvertida);
        lenient().when(terraIndigenaMapper.converter(any(TerraIndigena.class))).thenReturn(responseMock);

        TerraIndigenaResponseDTO resultado = terraIndigenaService.editar(requestDTO);

        assertNotNull(resultado);
        verify(terraIndigenaRepository, times(1)).findById(1L);
        verify(municipioRepository, times(1)).findById(2L);
        verify(terraIndigenaRepository, times(1)).save(terraConvertida);
    }

    @Test
    public void deveLancarExcecaoAoEditarTerraIndigenaInexistente() {
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(99L, "Vale Editado", 1L);
        when(terraIndigenaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.editar(requestDTO);
        });

        assertEquals("Id de terra indígena inválido ou não encontrado", erro.getMessage());
        verify(municipioRepository, never()).findById(anyLong());
        verify(terraIndigenaRepository, never()).save(any(TerraIndigena.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComMunicipioInexistente() {
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(1L, "Vale Editado", 99L);
        TerraIndigena terraExistente = new TerraIndigena();
        terraExistente.setId(1L);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terraExistente));
        when(municipioRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.editar(requestDTO);
        });

        assertEquals("Id de município inválido ou não encontrado", erro.getMessage());
        verify(terraIndigenaRepository, never()).save(any(TerraIndigena.class));
    }

    @Test
    public void deveExcluirTerraIndigenaComSucesso() {
        Long id = 1L;
        TerraIndigena terra = new TerraIndigena();
        terra.setId(id);

        when(terraIndigenaRepository.findById(id)).thenReturn(Optional.of(terra));
        doNothing().when(terraIndigenaRepository).deleteById(id);

        assertDoesNotThrow(() -> terraIndigenaService.excluir(id));

        verify(terraIndigenaRepository, times(1)).findById(id);
        verify(terraIndigenaRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirTerraIndigenaInexistente() {
        Long id = 99L;
        when(terraIndigenaRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.excluir(id);
        });

        assertEquals("Id de terra indígena inválido ou não encontrado", erro.getMessage());
        verify(terraIndigenaRepository, never()).deleteById(anyLong());
    }
}