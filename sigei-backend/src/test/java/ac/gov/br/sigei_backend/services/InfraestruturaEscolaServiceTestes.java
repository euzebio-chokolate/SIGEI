package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.mappers.InfraestruturaEscolaMapper;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.InfraestruturaEscolaRepository;
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
public class InfraestruturaEscolaServiceTestes {

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    InfraestruturaEscolaRepository infraestruturaRepository;

    @Mock
    InfraestruturaEscolaMapper infraestruturaMapper;

    @InjectMocks
    InfraestruturaEscolaService infraestruturaService;

    @Test
    public void deveSalvarInfraestruturaComSucesso() {
        InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
                null, 1L, 2024, 2005, 5, -8.0, -71.0, "Ativa", "Fluvial", true, true, false, true, true
        );

        Escola escola = new Escola();
        escola.setId(1L);

        InfraestruturaEscola infraConvertida = new InfraestruturaEscola();
        InfraestruturaEscola infraSalva = new InfraestruturaEscola();
        InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
        when(infraestruturaMapper.converter(requestDTO)).thenReturn(infraConvertida);
        when(infraestruturaRepository.save(any(InfraestruturaEscola.class))).thenReturn(infraSalva);
        lenient().when(infraestruturaMapper.converter(infraSalva)).thenReturn(responseMock);

        InfraestruturaEscolaResponseDTO resultado = infraestruturaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(escolaRepository, times(1)).findById(1L);
        verify(infraestruturaRepository, times(1)).save(infraConvertida);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComEscolaInvalida() {
        InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
                null, 99L, 2024, 2005, 5, -8.0, -71.0, "Ativa", "Fluvial", true, true, false, true, true
        );
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaService.salvar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
    }

    @Test
    public void deveBuscarTodasAsInfraestruturas() {
        List<InfraestruturaEscola> lista = new ArrayList<>();
        lista.add(new InfraestruturaEscola());
        lista.add(new InfraestruturaEscola());

        InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

        when(infraestruturaRepository.findAll()).thenReturn(lista);
        lenient().when(infraestruturaMapper.converter(any(InfraestruturaEscola.class))).thenReturn(responseMock);

        List<InfraestruturaEscolaResponseDTO> resultado = infraestruturaService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(infraestruturaRepository, times(1)).findAll();
        verify(infraestruturaMapper, times(2)).converter(any(InfraestruturaEscola.class));
    }

    @Test
    public void deveEditarInfraestruturaComSucesso() {
        InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
                1L, 2L, 2025, 2010, 8, -9.0, -72.0, "Ativa", "Terrestre", true, true, true, true, true
        );

        InfraestruturaEscola infraExistente = new InfraestruturaEscola();
        infraExistente.setId(1L);

        Escola escolaNova = new Escola();
        escolaNova.setId(2L);

        InfraestruturaEscola infraSalva = new InfraestruturaEscola();
        InfraestruturaEscolaResponseDTO responseMock = mock(InfraestruturaEscolaResponseDTO.class);

        when(infraestruturaRepository.findById(1L)).thenReturn(Optional.of(infraExistente));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escolaNova));
        doNothing().when(infraestruturaMapper).atualizar(requestDTO, infraExistente);
        when(infraestruturaRepository.save(any(InfraestruturaEscola.class))).thenReturn(infraSalva);
        lenient().when(infraestruturaMapper.converter(infraSalva)).thenReturn(responseMock);

        InfraestruturaEscolaResponseDTO resultado = infraestruturaService.editar(requestDTO);

        assertNotNull(resultado);
        verify(infraestruturaRepository, times(1)).findById(1L);
        verify(escolaRepository, times(1)).findById(2L);
        verify(infraestruturaRepository, times(1)).save(infraExistente);
    }

    @Test
    public void deveLancarExcecaoAoEditarInfraestruturaInexistente() {
        InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
                99L, 1L, 2025, 2010, 8, -9.0, -72.0, "Ativa", "Terrestre", true, true, true, true, true
        );
        when(infraestruturaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaService.editar(requestDTO));

        assertEquals("Infraestrutura não encontrada", erro.getMessage());
        verify(escolaRepository, never()).findById(anyLong());
        verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComEscolaInexistente() {
        InfraestruturaEscolaRequestDTO requestDTO = new InfraestruturaEscolaRequestDTO(
                1L, 99L, 2025, 2010, 8, -9.0, -72.0, "Ativa", "Terrestre", true, true, true, true, true
        );

        InfraestruturaEscola infraExistente = new InfraestruturaEscola();
        infraExistente.setId(1L);

        when(infraestruturaRepository.findById(1L)).thenReturn(Optional.of(infraExistente));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaService.editar(requestDTO));

        assertEquals("Escola não encontrada", erro.getMessage());
        verify(infraestruturaRepository, never()).save(any(InfraestruturaEscola.class));
    }

    @Test
    public void deveExcluirInfraestruturaComSucesso() {
        Long id = 1L;
        InfraestruturaEscola infra = new InfraestruturaEscola();
        infra.setId(id);

        when(infraestruturaRepository.findById(id)).thenReturn(Optional.of(infra));
        doNothing().when(infraestruturaRepository).deleteById(id);

        assertDoesNotThrow(() -> infraestruturaService.excluir(id));

        verify(infraestruturaRepository, times(1)).findById(id);
        verify(infraestruturaRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirInfraestruturaInexistente() {
        Long id = 99L;
        when(infraestruturaRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> infraestruturaService.excluir(id));

        assertEquals("Infraestrutura não encontrada", erro.getMessage());
        verify(infraestruturaRepository, never()).deleteById(anyLong());
    }
}