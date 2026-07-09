package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.EquipeEscolaMapper;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.repository.EquipeEscolaRepository;
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
public class EquipeEscolaServiceTestes {

    @Mock
    EquipeEscolaRepository equipeEscolaRepository;

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    EquipeEscolaMapper equipeEscolaMapper;

    @InjectMocks
    EquipeEscolaService equipeEscolaService;

    @Test
    public void deveSalvarEquipeEscolaComSucesso() {
        EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(null, 1L, 2024, true, false, true, false, false, true);

        Escola escola = new Escola();
        escola.setId(1L);

        EquipeEscola equipeConvertida = new EquipeEscola();
        EquipeEscola equipeSalva = new EquipeEscola();
        EquipeEscolaResponseDTO responseMock = mock(EquipeEscolaResponseDTO.class);

        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
        when(equipeEscolaMapper.converter(requestDTO)).thenReturn(equipeConvertida);
        when(equipeEscolaRepository.save(any(EquipeEscola.class))).thenReturn(equipeSalva);
        lenient().when(equipeEscolaMapper.converter(equipeSalva)).thenReturn(responseMock);

        EquipeEscolaResponseDTO resultado = equipeEscolaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(escolaRepository, times(1)).findById(1L);
        verify(equipeEscolaRepository, times(1)).save(equipeConvertida);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComEscolaInvalida() {
        EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(null, 99L, 2024, true, false, true, false, false, true);
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.salvar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(equipeEscolaRepository, never()).save(any(EquipeEscola.class));
    }

    @Test
    public void deveEditarEquipeEscolaComSucesso() {
        EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(1L, 2L, 2025, true, true, true, false, false, true);

        EquipeEscola equipeExistente = new EquipeEscola();
        equipeExistente.setId(1L);

        Escola escolaNova = new Escola();
        escolaNova.setId(2L);

        EquipeEscola equipeSalva = new EquipeEscola();
        EquipeEscolaResponseDTO responseMock = mock(EquipeEscolaResponseDTO.class);

        when(equipeEscolaRepository.findById(1L)).thenReturn(Optional.of(equipeExistente));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escolaNova));
        doNothing().when(equipeEscolaMapper).atualizar(requestDTO, equipeExistente);
        when(equipeEscolaRepository.save(any(EquipeEscola.class))).thenReturn(equipeSalva);
        lenient().when(equipeEscolaMapper.converter(equipeSalva)).thenReturn(responseMock);

        EquipeEscolaResponseDTO resultado = equipeEscolaService.editar(requestDTO);

        assertNotNull(resultado);
        verify(equipeEscolaRepository, times(1)).findById(1L);
        verify(escolaRepository, times(1)).findById(2L);
        verify(equipeEscolaRepository, times(1)).save(equipeExistente);
    }

    @Test
    public void deveLancarExcecaoAoEditarEquipeEscolaInexistente() {
        EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(99L, 1L, 2024, true, false, true, false, false, true);
        when(equipeEscolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.editar(requestDTO));

        assertEquals("Equipe escolar não encontrada", erro.getMessage());
        verify(escolaRepository, never()).findById(anyLong());
        verify(equipeEscolaRepository, never()).save(any(EquipeEscola.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComEscolaInexistente() {
        EquipeEscolaRequestDTO requestDTO = new EquipeEscolaRequestDTO(1L, 99L, 2024, true, false, true, false, false, true);

        EquipeEscola equipeExistente = new EquipeEscola();
        equipeExistente.setId(1L);

        when(equipeEscolaRepository.findById(1L)).thenReturn(Optional.of(equipeExistente));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> equipeEscolaService.editar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(equipeEscolaRepository, never()).save(any(EquipeEscola.class));
    }
}