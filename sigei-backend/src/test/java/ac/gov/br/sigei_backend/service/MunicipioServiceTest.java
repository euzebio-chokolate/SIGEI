package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.mappers.MunicipioMapper;
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
public class MunicipioServiceTest {

    @Mock
    MunicipioRepository municipioRepository;

    @Mock
    MunicipioMapper municipioMapper;

    @InjectMocks
    MunicipioService municipioService;

    @Test
    public void deveSalvarMunicipioComSucesso() {
        MunicipioRequestDTO requestDTO = new MunicipioRequestDTO(null, "Rio Branco");
        Municipio municipioConvertido = new Municipio();
        MunicipioResponseDTO responseMock = mock(MunicipioResponseDTO.class);

        when(municipioMapper.converter(requestDTO)).thenReturn(municipioConvertido);
        when(municipioRepository.save(any(Municipio.class))).thenReturn(municipioConvertido);
        lenient().when(municipioMapper.converter(any(Municipio.class))).thenReturn(responseMock);

        MunicipioResponseDTO resultado = municipioService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(municipioRepository, times(1)).save(municipioConvertido);
    }

    @Test
    public void deveBuscarTodosOsMunicipios() {
        List<Municipio> lista = new ArrayList<>();
        lista.add(new Municipio());
        lista.add(new Municipio());

        MunicipioResponseDTO responseMock = mock(MunicipioResponseDTO.class);

        when(municipioRepository.findAll()).thenReturn(lista);
        lenient().when(municipioMapper.converter(any(Municipio.class))).thenReturn(responseMock);

        List<MunicipioResponseDTO> resultado = municipioService.buscarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(municipioRepository, times(1)).findAll();
        verify(municipioMapper, times(2)).converter(any(Municipio.class));
    }

    @Test
    public void deveEditarMunicipioComSucesso() {
        MunicipioRequestDTO requestDTO = new MunicipioRequestDTO(1L, "Rio Branco Editado");

        Municipio municipioExistente = new Municipio();
        municipioExistente.setId(1L);

        Municipio municipioConvertido = new Municipio();
        MunicipioResponseDTO responseMock = mock(MunicipioResponseDTO.class);

        when(municipioRepository.findById(1L)).thenReturn(Optional.of(municipioExistente));
        when(municipioMapper.converter(requestDTO)).thenReturn(municipioConvertido);
        when(municipioRepository.save(any(Municipio.class))).thenReturn(municipioConvertido);
        lenient().when(municipioMapper.converter(any(Municipio.class))).thenReturn(responseMock);

        MunicipioResponseDTO resultado = municipioService.editar(requestDTO);

        assertNotNull(resultado);
        verify(municipioRepository, times(1)).findById(1L);
        verify(municipioRepository, times(1)).save(municipioConvertido);
    }

    @Test
    public void deveLancarExcecaoAoEditarMunicipioInexistente() {
        MunicipioRequestDTO requestDTO = new MunicipioRequestDTO(99L, "Rio Branco");
        when(municipioRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            municipioService.editar(requestDTO);
        });

        assertEquals("Id de municipio inválido ou não encontrado", erro.getMessage());
        verify(municipioRepository, never()).save(any(Municipio.class));
    }

    @Test
    public void deveExcluirMunicipioComSucesso() {
        Long id = 1L;
        Municipio municipio = new Municipio();
        municipio.setId(id);

        when(municipioRepository.findById(id)).thenReturn(Optional.of(municipio));
        doNothing().when(municipioRepository).deleteById(id);

        assertDoesNotThrow(() -> municipioService.excluir(id));

        verify(municipioRepository, times(1)).findById(id);
        verify(municipioRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirMunicipioInexistente() {
        Long id = 99L;
        when(municipioRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> {
            municipioService.excluir(id);
        });

        assertEquals("Id de municipio inválido ou não encontrado", erro.getMessage());
        verify(municipioRepository, never()).deleteById(anyLong());
    }
}