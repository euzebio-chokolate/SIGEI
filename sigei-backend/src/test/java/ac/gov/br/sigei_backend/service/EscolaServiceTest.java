package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.repository.AldeiaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.mappers.EscolaMapper;
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
public class EscolaServiceTest {

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    AldeiaRepository aldeiaRepository;

    @Mock
    EscolaMapper escolaMapper;

    @InjectMocks
    EscolaService escolaService;

    @Test
    public void deveSalvarEscolaComSedeComSucesso() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(null, "123", "Escola Nova", "Polo", 2L, 1L);

        Aldeia aldeia = new Aldeia();
        Escola escolaSede = new Escola();
        Escola escolaConvertida = new Escola();
        Escola escolaSalva = new Escola();
        EscolaResponseDTO responseMock = mock(EscolaResponseDTO.class);

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeia));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escolaSede));
        when(escolaMapper.converter(requestDTO)).thenReturn(escolaConvertida);
        when(escolaRepository.save(any(Escola.class))).thenReturn(escolaSalva);
        lenient().when(escolaMapper.converter(escolaSalva)).thenReturn(responseMock);

        EscolaResponseDTO resultado = escolaService.salvar(requestDTO);

        assertNotNull(resultado);
        verify(escolaRepository, times(1)).save(escolaConvertida);
    }

    @Test
    public void deveSalvarEscolaSemSedeComSucesso() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(null, "123", "Escola Nova", "Polo", null, 1L);

        Aldeia aldeia = new Aldeia();
        Escola escolaConvertida = new Escola();
        Escola escolaSalva = new Escola();

        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeia));
        when(escolaMapper.converter(requestDTO)).thenReturn(escolaConvertida);
        when(escolaRepository.save(any(Escola.class))).thenReturn(escolaSalva);

        EscolaResponseDTO resultado = escolaService.salvar(requestDTO);

        verify(escolaRepository, never()).findById(anyLong());
        verify(escolaRepository, times(1)).save(escolaConvertida);
    }

    @Test
    public void deveLancarExcecaoAoSalvarComAldeiaInvalida() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(null, "123", "Escola Nova", "Polo", null, 99L);
        when(aldeiaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> escolaService.salvar(requestDTO));
        assertEquals("Aldeia não encontrada", erro.getMessage());
        verify(escolaRepository, never()).save(any(Escola.class));
    }

    @Test
    public void deveLancarExcecaoAoSalvarComEscolaSedeInvalida() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(null, "123", "Escola Nova", "Polo", 99L, 1L);
        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(new Aldeia()));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> escolaService.salvar(requestDTO));
        assertEquals("Escola sede não encontrada", erro.getMessage());
        verify(escolaRepository, never()).save(any(Escola.class));
    }

    @Test
    public void deveBuscarTodasAsEscolas() {
        List<Escola> lista = new ArrayList<>();
        lista.add(new Escola());
        lista.add(new Escola());

        EscolaResponseDTO responseMock = mock(EscolaResponseDTO.class);

        when(escolaRepository.findAll()).thenReturn(lista);
        lenient().when(escolaMapper.converter(any(Escola.class))).thenReturn(responseMock);

        List<EscolaResponseDTO> resultado = escolaService.buscarTudo();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(escolaRepository, times(1)).findAll();
        verify(escolaMapper, times(2)).converter(any(Escola.class));
    }

    @Test
    public void deveEditarEscolaComSedeComSucesso() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(1L, "123", "Escola Editada", "Polo", 2L, 1L);

        Escola escolaExistente = new Escola();
        Aldeia aldeia = new Aldeia();
        Escola escolaSede = new Escola();
        Escola escolaSalva = new Escola();
        EscolaResponseDTO responseMock = mock(EscolaResponseDTO.class);

        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escolaExistente));
        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(aldeia));
        when(escolaRepository.findById(2L)).thenReturn(Optional.of(escolaSede));
        doNothing().when(escolaMapper).atualizar(requestDTO, escolaExistente);
        when(escolaRepository.save(any(Escola.class))).thenReturn(escolaSalva);
        lenient().when(escolaMapper.converter(escolaSalva)).thenReturn(responseMock);

        EscolaResponseDTO resultado = escolaService.editar(requestDTO);

        assertNotNull(resultado);
        verify(escolaRepository, times(1)).save(escolaExistente);
    }

    @Test
    public void deveEditarEscolaSemSedeComSucesso() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(1L, "123", "Escola Editada", "Polo", null, 1L);

        Escola escolaExistente = new Escola();
        when(escolaRepository.findById(1L)).thenReturn(Optional.of(escolaExistente));
        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(new Aldeia()));
        doNothing().when(escolaMapper).atualizar(requestDTO, escolaExistente);
        when(escolaRepository.save(any(Escola.class))).thenReturn(new Escola());

        escolaService.editar(requestDTO);

        verify(escolaRepository, times(1)).findById(1L);
        verify(escolaRepository, times(1)).save(escolaExistente);
    }

    @Test
    public void deveLancarExcecaoAoEditarEscolaInexistente() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(99L, "123", "Escola Editada", "Polo", 2L, 1L);
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> escolaService.editar(requestDTO));
        assertEquals("Escola não encontrada", erro.getMessage());
        verify(escolaRepository, never()).save(any(Escola.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComAldeiaInexistente() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(1L, "123", "Escola Editada", "Polo", 2L, 99L);
        when(escolaRepository.findById(1L)).thenReturn(Optional.of(new Escola()));
        when(aldeiaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> escolaService.editar(requestDTO));
        assertEquals("Aldeia não encontrada", erro.getMessage());
        verify(escolaRepository, never()).save(any(Escola.class));
    }

    @Test
    public void deveLancarExcecaoAoEditarComEscolaSedeInexistente() {
        EscolaRequestDTO requestDTO = new EscolaRequestDTO(1L, "123", "Escola Editada", "Polo", 99L, 1L);
        when(escolaRepository.findById(1L)).thenReturn(Optional.of(new Escola()));
        when(aldeiaRepository.findById(1L)).thenReturn(Optional.of(new Aldeia()));
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> escolaService.editar(requestDTO));
        assertEquals("Escola sede não encontrada", erro.getMessage());
        verify(escolaRepository, never()).save(any(Escola.class));
    }

    @Test
    public void deveExcluirEscolaComSucesso() {
        Long id = 1L;
        Escola escola = new Escola();
        escola.setId(id);

        when(escolaRepository.findById(id)).thenReturn(Optional.of(escola));
        doNothing().when(escolaRepository).deleteById(id);

        assertDoesNotThrow(() -> escolaService.excluir(id));
        verify(escolaRepository, times(1)).findById(id);
        verify(escolaRepository, times(1)).deleteById(id);
    }

    @Test
    public void deveLancarExcecaoAoExcluirEscolaInexistente() {
        when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> escolaService.excluir(99L));
        assertEquals("Id de escola inválido ou não encontrado", erro.getMessage());
        verify(escolaRepository, never()).deleteById(anyLong());
    }
}