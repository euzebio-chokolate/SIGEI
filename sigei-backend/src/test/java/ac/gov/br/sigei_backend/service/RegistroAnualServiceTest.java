package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.dto.response.PaginaDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.mappers.RegistroAnualMapper;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroAnualServiceTest {

    @Mock
    EscolaRepository escolaRepository;

    @Mock
    AnoReferenciaRepository anoReferenciaRepository;

    @Mock
    RegistroAnualRepository registroAnualRepository;

    @Mock
    RegistroAnualMapper registroAnualMapper;

    @InjectMocks
    RegistroAnualService registroAnualService;

    @Nested
    class Salvar {

        @Test
        void deveSalvarRegistroAnualComSucesso() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 1L, 2L, "Estadual", "Urbana", null);

            Escola escola = new Escola();
            escola.setId(1L);

            AnoReferencia anoReferencia = new AnoReferencia();
            anoReferencia.setId(2L);

            RegistroAnual registroConvertido = new RegistroAnual();
            RegistroAnual registroSalvo = new RegistroAnual();
            RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

            when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
            when(anoReferenciaRepository.findById(2L)).thenReturn(Optional.of(anoReferencia));
            when(registroAnualRepository.existsByEscolaAndAnoReferencia(escola, anoReferencia)).thenReturn(false);
            when(registroAnualMapper.converter(requestDTO)).thenReturn(registroConvertido);
            when(registroAnualRepository.save(any(RegistroAnual.class))).thenReturn(registroSalvo);
            lenient().when(registroAnualMapper.converter(registroSalvo)).thenReturn(responseMock);

            RegistroAnualResponseDTO resultado = registroAnualService.salvar(requestDTO);

            assertNotNull(resultado);
            verify(escolaRepository, times(1)).findById(1L);
            verify(anoReferenciaRepository, times(1)).findById(2L);
            verify(registroAnualRepository, times(1)).existsByEscolaAndAnoReferencia(escola, anoReferencia);
            verify(registroAnualRepository, times(1)).save(registroConvertido);
        }

        @Test
        void deveGravarRedeELocalPeloNomeDaOpcao() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 1L, 2L, " estadual ", "Urbana", null);
            RegistroAnual registroConvertido = registroComOpcoes(requestDTO);

            registroAnualService.salvar(requestDTO);

            assertEquals("ESTADUAL", registroConvertido.getRede());
            assertEquals("URBANA", registroConvertido.getLocalFuncionamento());
        }

        @Test
        void deveRecusarRedeOuLocalForaDasOpcoes() {
            RegistroAnualRequestDTO redeInvalida = new RegistroAnualRequestDTO(null, 1L, 2L, "Particular", "Rural", null);
            registroComOpcoes(redeInvalida);

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> registroAnualService.salvar(redeInvalida));

            assertEquals("Rede inválida: use ESTADUAL, MUNICIPAL.", erro.getMessage());
            verify(registroAnualRepository, never()).save(any(RegistroAnual.class));

            RegistroAnualRequestDTO localInvalido = new RegistroAnualRequestDTO(null, 1L, 2L, "Municipal", "Prédio próprio", null);
            registroComOpcoes(localInvalido);

            assertThrows(IllegalArgumentException.class, () -> registroAnualService.salvar(localInvalido));
            verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
        }

        /** Prepara os mocks do salvar; o mapper devolve a entidade com rede e local do DTO. */
        private RegistroAnual registroComOpcoes(RegistroAnualRequestDTO dto) {
            Escola escola = new Escola();
            AnoReferencia anoReferencia = new AnoReferencia();
            RegistroAnual registro = new RegistroAnual();
            registro.setRede(dto.rede());
            registro.setLocalFuncionamento(dto.localFuncionamento());

            when(escolaRepository.findById(dto.escolaId())).thenReturn(Optional.of(escola));
            when(anoReferenciaRepository.findById(dto.anoReferenciaId())).thenReturn(Optional.of(anoReferencia));
            when(registroAnualMapper.converter(dto)).thenReturn(registro);
            lenient().when(registroAnualRepository.save(registro)).thenReturn(registro);
            return registro;
        }

        @Test
        void deveLancarExcecaoAoSalvarComEscolaInvalida() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 99L, 2L, "Estadual", "Urbana", null);

            when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

            NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.salvar(requestDTO));

            assertEquals("Escola não encontrada", erro.getMessage());
            verify(anoReferenciaRepository, never()).findById(anyLong());
            verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
        }

        @Test
        void deveLancarExcecaoAoSalvarComAnoReferenciaInvalido() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 1L, 99L, "Estadual", "Urbana", null);
            Escola escola = new Escola();
            escola.setId(1L);

            when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
            when(anoReferenciaRepository.findById(99L)).thenReturn(Optional.empty());

            NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.salvar(requestDTO));

            assertEquals("Ano de referência não encontrado", erro.getMessage());
            verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
        }

        @Test
        void deveLancarExcecaoAoSalvarRegistroDuplicado() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(null, 1L, 2L, "Estadual", "Urbana", null);

            Escola escola = new Escola();
            escola.setId(1L);

            AnoReferencia anoReferencia = new AnoReferencia();
            anoReferencia.setId(2L);

            when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
            when(anoReferenciaRepository.findById(2L)).thenReturn(Optional.of(anoReferencia));
            when(registroAnualRepository.existsByEscolaAndAnoReferencia(escola, anoReferencia)).thenReturn(true);

            RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> registroAnualService.salvar(requestDTO));

            assertEquals("Já existe um registro anual para essa escola neste ano.", erro.getMessage());
            verify(registroAnualRepository, never()).save(any(RegistroAnual.class));
        }
    }

    @Nested
    class Buscar {

        @Test
        void deveBuscarRegistrosAnuaisPaginados() {
            List<RegistroAnual> lista = List.of(new RegistroAnual(), new RegistroAnual());
            Pageable pageable = PageRequest.of(1, 2);

            RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

            when(registroAnualRepository.buscar(2025, null, 3L, pageable)).thenReturn(new PageImpl<>(lista, pageable, 5));
            when(registroAnualMapper.converter(any(RegistroAnual.class))).thenReturn(responseMock);

            PaginaDTO<RegistroAnualResponseDTO> resultado = registroAnualService.buscar(2025, null, 3L, 1, 2);

            assertEquals(List.of(responseMock, responseMock), resultado.itens());
            assertEquals(1, resultado.pagina());
            assertEquals(2, resultado.tamanho());
            assertEquals(5, resultado.totalItens());
            assertEquals(3, resultado.totalPaginas());
        }

        @Test
        void deveRecusarPaginacaoInvalida() {
            assertThrows(IllegalArgumentException.class, () -> registroAnualService.buscar(null, null, null, -1, 20));
            assertThrows(IllegalArgumentException.class, () -> registroAnualService.buscar(null, null, null, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> registroAnualService.buscar(null, null, null, 0, 101));
            verifyNoInteractions(registroAnualRepository);
        }

        @Test
        void deveBuscarPorEscolaEAnoComSucesso() {
            Escola escola = new Escola();
            escola.setId(1L);

            RegistroAnual registro = new RegistroAnual();
            RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

            when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
            when(registroAnualRepository.findByEscolaAndAnoReferencia_Ano(escola, 2026)).thenReturn(Optional.of(registro));
            when(registroAnualMapper.converter(registro)).thenReturn(responseMock);

            RegistroAnualResponseDTO resultado = registroAnualService.buscarPorEscolaEAno(1L, 2026);

            assertNotNull(resultado);
            verify(escolaRepository, times(1)).findById(1L);
            verify(registroAnualRepository, times(1)).findByEscolaAndAnoReferencia_Ano(escola, 2026);
        }

        @Test
        void deveLancarExcecaoAoBuscarPorEscolaEAnoComEscolaInvalida() {
            when(escolaRepository.findById(99L)).thenReturn(Optional.empty());

            NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.buscarPorEscolaEAno(99L, 2026));

            assertEquals("Escola não encontrada", erro.getMessage());
            verify(registroAnualRepository, never()).findByEscolaAndAnoReferencia_Ano(any(), anyInt());
        }

        @Test
        void deveLancarExcecaoAoBuscarPorEscolaEAnoInexistente() {
            Escola escola = new Escola();
            escola.setId(1L);

            when(escolaRepository.findById(1L)).thenReturn(Optional.of(escola));
            when(registroAnualRepository.findByEscolaAndAnoReferencia_Ano(escola, 2026)).thenReturn(Optional.empty());

            NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.buscarPorEscolaEAno(1L, 2026));

            assertEquals("Registro anual não encontrado para a escola no ano 2026", erro.getMessage());
        }

        @Test
        void deveBuscarPorAno() {
            List<RegistroAnual> lista = new ArrayList<>();
            lista.add(new RegistroAnual());

            RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

            when(registroAnualRepository.findByAnoReferencia_Ano(2026)).thenReturn(lista);
            lenient().when(registroAnualMapper.converter(any(RegistroAnual.class))).thenReturn(responseMock);

            List<RegistroAnualResponseDTO> resultado = registroAnualService.buscarPorAno(2026);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(registroAnualRepository, times(1)).findByAnoReferencia_Ano(2026);
        }
    }

    @Nested
    class Editar {

        @Test
        void deveEditarRegistroAnualComSucesso() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(1L, 2L, 3L, "Estadual", "Urbana", null);

            RegistroAnual registroExistente = new RegistroAnual();
            registroExistente.setId(1L);

            Escola escola = new Escola();
            escola.setId(2L);

            AnoReferencia anoReferencia = new AnoReferencia();
            anoReferencia.setId(3L);

            RegistroAnual registroSalvo = new RegistroAnual();
            RegistroAnualResponseDTO responseMock = mock(RegistroAnualResponseDTO.class);

            when(registroAnualRepository.findById(1L)).thenReturn(Optional.of(registroExistente));
            when(escolaRepository.findById(2L)).thenReturn(Optional.of(escola));
            when(anoReferenciaRepository.findById(3L)).thenReturn(Optional.of(anoReferencia));
            doNothing().when(registroAnualMapper).atualizar(requestDTO, registroExistente);
            when(registroAnualRepository.save(any(RegistroAnual.class))).thenReturn(registroSalvo);
            lenient().when(registroAnualMapper.converter(registroSalvo)).thenReturn(responseMock);

            RegistroAnualResponseDTO resultado = registroAnualService.editar(requestDTO.id(), requestDTO);

            assertNotNull(resultado);
            verify(registroAnualRepository, times(1)).findById(1L);
            verify(escolaRepository, times(1)).findById(2L);
            verify(anoReferenciaRepository, times(1)).findById(3L);
            verify(registroAnualRepository, times(1)).save(registroExistente);
        }

        @Test
        void deveLancarExcecaoAoEditarRegistroInexistente() {
            RegistroAnualRequestDTO requestDTO = new RegistroAnualRequestDTO(99L, 1L, 2L, "Estadual", "Urbana", null);
            when(registroAnualRepository.findById(99L)).thenReturn(Optional.empty());

            NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.editar(requestDTO.id(), requestDTO));

            assertEquals("Registro anual não encontrado", erro.getMessage());
            verify(escolaRepository, never()).findById(anyLong());
            verify(registroAnualRepository, never()).save(any());
        }
    }

    @Nested
    class Excluir {

        @Test
        void deveExcluirRegistroAnualComSucesso() {
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
        void deveLancarExcecaoAoExcluirRegistroInexistente() {
            Long id = 99L;
            when(registroAnualRepository.findById(id)).thenReturn(Optional.empty());

            NoSuchElementException erro = assertThrows(NoSuchElementException.class, () -> registroAnualService.excluir(id));

            assertEquals("Registro anual não encontrado", erro.getMessage());
            verify(registroAnualRepository, never()).deleteById(anyLong());
        }
    }
}
