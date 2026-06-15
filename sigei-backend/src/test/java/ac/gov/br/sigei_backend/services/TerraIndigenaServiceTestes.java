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

    // TESTES DO MÉTODO: salvar()
    @Test
    public void deveSalvarTerraIndigenaComSucesso() {
        // 1. Arrange
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(null, "Vale do Javari", 1L);
        Municipio municipio = new Municipio();
        municipio.setId(1L);

        TerraIndigena terraSalva = new TerraIndigena();
        terraSalva.setId(1L);
        terraSalva.setNome("Vale do Javari");
        terraSalva.setMunicipio(municipio);

        TerraIndigenaResponseDTO responseMock = mock(TerraIndigenaResponseDTO.class);

        when(municipioRepository.findById(1L)).thenReturn(Optional.of(municipio));
        when(terraIndigenaRepository.save(any(TerraIndigena.class))).thenReturn(terraSalva);
        when(terraIndigenaMapper.converter(any(TerraIndigena.class))).thenReturn(responseMock);

        // 2. Act
        TerraIndigenaResponseDTO resultado = terraIndigenaService.salvar(requestDTO);

        // 3. Assert
        assertNotNull(resultado);
        verify(municipioRepository, times(1)).findById(1L);
        verify(terraIndigenaRepository, times(1)).save(any(TerraIndigena.class));
    }

    @Test
    public void deveLancarExcecaoAoSalvarComMunicipioInvalido() {
        // 1. Arrange
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(null, "Vale do Javari", 99L);
        when(municipioRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.salvar(requestDTO);
        });

        assertEquals("Id de municipio inválido ou não encontrado", erro.getMessage());
        verify(terraIndigenaRepository, never()).save(any(TerraIndigena.class));
    }

    // ==========================================================
    // TESTES DO MÉTODO: buscarTudo()
    // ==========================================================

    @Test
    public void deveBuscarTodasAsTerrasIndigenas() {
        // 1. Arrange
        List<TerraIndigena> lista = new ArrayList<>();
        lista.add(new TerraIndigena());
        lista.add(new TerraIndigena());

        TerraIndigenaResponseDTO responseMock = mock(TerraIndigenaResponseDTO.class);

        when(terraIndigenaRepository.findAll()).thenReturn(lista);
        when(terraIndigenaMapper.converter(any(TerraIndigena.class))).thenReturn(responseMock);

        // 2. Act
        List<TerraIndigenaResponseDTO> resultado = terraIndigenaService.buscarTudo();

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(terraIndigenaRepository, times(1)).findAll();
        verify(terraIndigenaMapper, times(2)).converter(any(TerraIndigena.class));
    }

    // ==========================================================
    // TESTES DO MÉTODO: editar()
    // ==========================================================

    @Test
    public void deveEditarTerraIndigenaComSucesso() {
        // 1. Arrange
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(1L, "Terra Editada", 2L);

        TerraIndigena terraAntiga = new TerraIndigena();
        terraAntiga.setId(1L);
        terraAntiga.setNome("Nome Antigo");

        Municipio municipioNovo = new Municipio();
        municipioNovo.setId(2L);

        TerraIndigenaResponseDTO responseMock = mock(TerraIndigenaResponseDTO.class);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terraAntiga));
        when(municipioRepository.findById(2L)).thenReturn(Optional.of(municipioNovo));

        // No seu código o save é chamado duas vezes no editar, por isso usamos any()
        when(terraIndigenaRepository.save(any(TerraIndigena.class))).thenReturn(terraAntiga);
        when(terraIndigenaMapper.converter(any(TerraIndigena.class))).thenReturn(responseMock);

        // 2. Act
        TerraIndigenaResponseDTO resultado = terraIndigenaService.editar(requestDTO);

        // 3. Assert
        assertNotNull(resultado);
        verify(terraIndigenaRepository, times(1)).findById(1L);
        verify(municipioRepository, times(1)).findById(2L);
        verify(terraIndigenaRepository, times(2)).save(any(TerraIndigena.class)); // Verifica os dois saves do seu código
    }

    @Test
    public void deveLancarExcecaoAoEditarTerraIndigenaInexistente() {
        // 1. Arrange
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(99L, "Terra Editada", 1L);
        when(terraIndigenaRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.editar(requestDTO);
        });

        assertEquals("Id de terra indígena inválido ou não encontrado", erro.getMessage());
        verify(municipioRepository, never()).findById(anyLong()); // Não deve nem tentar buscar o município
        verify(terraIndigenaRepository, never()).save(any(TerraIndigena.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComMunicipioInexistente() {
        // 1. Arrange
        TerraIndigenaRequestDTO requestDTO = new TerraIndigenaRequestDTO(1L, "Terra Editada", 99L);

        TerraIndigena terraExistente = new TerraIndigena();
        terraExistente.setId(1L);

        when(terraIndigenaRepository.findById(1L)).thenReturn(Optional.of(terraExistente));
        when(municipioRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            terraIndigenaService.editar(requestDTO);
        });

        assertEquals("Id de município inválido ou não encontrado", erro.getMessage());
        verify(terraIndigenaRepository, never()).save(any(TerraIndigena.class));
    }

    // ==========================================================
    // TESTES DO MÉTODO: excluir()
    // ==========================================================

    @Test
    public void deveExcluirTerraIndigenaComSucesso() {
        // 1. Arrange
        Long id = 1L;
        TerraIndigena terra = new TerraIndigena();
        terra.setId(id);

        when(terraIndigenaRepository.findById(id)).thenReturn(Optional.of(terra));
        doNothing().when(terraIndigenaRepository).deleteById(id);

        // 2. Act
        assertDoesNotThrow(() -> terraIndigenaService.excluir(id));

        // 3. Assert
        verify(terraIndigenaRepository, times(1)).findById(id);
        verify(terraIndigenaRepository, times(1)).deleteById(id);
    }
}