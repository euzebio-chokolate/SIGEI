package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.mappers.RegistroAnualMapper;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
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
public class RegistroAnualServiceTestes {

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    RegistroAnualRepository registroAnualRepository;

    @Mock
    RegistroAnualMapper registroAnualMapper;

    @InjectMocks
    RegistroAnualService registroAnualService;

    @Test
    public void deveSalvarRegistroAnualComSucesso() {
        RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 1L, 2024, "Estadual", "Prédio Próprio");

        Escola escola = new Escola();
        escola.setId(1L);

        RegistroAnual registroConvertido = new RegistroAnual();
        RegistroAnual registroSalvo = new RegistroAnual();
        RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
        when(registroAnualMapper.converter(requestDTO)).thenReturn(registroConvertido);
        when(registroAnualRepository.save(any(RegistroAnual.class))).thenReturn(registroSalvo);
        lenient().when(registroAnualMapper.converter(registroSalvo)).thenReturn(responseMock);

        RegistroAnualResponseDTO resultado = registroAnualService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(escolaRepository, times(1)).findById(1L);
        verify(registroAnualRepository, times(1)).save(registroConvertido);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComEscolaInvalida() {
        RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 99L, 2024, "Estadual", "Prédio Próprio");
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.salvar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
    }

    @Test
    public void deveBuscarTodosOsRegistrosAnuais() {
        List<RegistroAnual> lista = new ArrayList<>();
        lista.add(new RegistroAnual());
        lista.add(new RegistroAnual());

        RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

        when(registroAnualRepository.findAll()).thenReturn(lista);
        lenient().when(registroAnualMapper.converter(any(RegistroAnual.class))).thenReturn(responseMock);

        List<RegistroAnualResponseDTO> resultado = registroAnualService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(registroAnualRepository, times(1)).findAll();
    }

    @Test
    public void deveEditarRegistroAnualComSucesso() {
        RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(1L, 2L, 2025, "Municipal", "Alugado");

        RegistroAnual registroExistente = new RegistroAnual();
        registroExistente.setId(1L);

        Escola escolaNova = new Escola();
        escolaNova.setId(2L);

        RegistroAnual registroSalvo = new RegistroAnual();
        RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

        when(registroAnualRepository.findById(1L)).thenReturn(Optional.of(registroExistente));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escolaNova));
        doNothing().when(registroAnualMapper).atualizar(requestDTO, registroExistente);
        when(registroAnualRepository.save(any(RegistroAnual.class))).thenReturn(registroSalvo);
        lenient().when(registroAnualMapper.converter(registroSalvo)).thenReturn(responseMock);

        RegistroAnualResponseDTO resultado = registroAnualService.editar(requestDTO);

        assertNotNull(resultado);
        verify(registroAnualRepository, times(1)).findById(1L);
        verify(escolaRepository, times(1)).findById(2L);
        verify(registroAnualRepository, times(1)).save(registroExistente);
    }

    @Test
    public void deveLancarExcecaoAoEditarRegistroAnualInexistente() {
        RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(99L, 1L, 2025, "Municipal", "Alugado");
        when(registroAnualRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.editar(requestDTO));

        assertEquals("Registro anual não encontrado", erro.getMessage());
        verify(escolaRepository, never()).findById(anyLong());
        verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComEscolaInexistente() {
        RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(1L, 99L, 2025, "Municipal", "Alugado");

        RegistroAnual registroExistente = new RegistroAnual();
        registroExistente.setId(1L);

        when(registroAnualRepository.findById(1L)).thenReturn(Optional.of(registroExistente));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.editar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
    }

    @Test
    public void deveExcluirRegistroAnualComSucesso() {
        Long id = 1L;
        RegistroAnual registro = new RegistroAnual();
        registro.setId(id);

        when(registroAnualRepository.findById(id)).thenReturn(Optional.of(registro));
        doNothing().when(registroAnualRepository).deleteById(id);

        assertDoesNotThrow(() -> registroAnualService.excluir(id));

        verify(registroAnualRepository, times(1)).findById(id);
        verify(registroAnualRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirRegistroAnualInexistente() {
        Long id = 99L;
        when(registroAnualRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.excluir(id));

        assertEquals("Registro anual não encontrado", erro.getMessage());
        verify(registroAnualRepository, never()).deleteById(anyLong());
    }
}