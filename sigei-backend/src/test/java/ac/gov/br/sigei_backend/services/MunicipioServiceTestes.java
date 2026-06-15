package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.utils.MunicipioMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MunicipioServiceTestes {

    @Mock
    MunicipioRepository municipioRepository;

    @Mock
    MunicipioMapper municipioMapper;

    @InjectMocks
    MunicipioService municipioService;

    @Test
    public void deveSalvarMunicipio() {
        MunicipioRequestDTO requestDTO = new MunicipioRequestDTO(null, "Rio Branco");

        Municipio municipioFake = new Municipio();
        municipioFake.setNome("Rio Branco");

        MunicipioResponseDTO responseDTO = new MunicipioResponseDTO(1L, "Rio Branco");

        when(municipioMapper.converter(requestDTO)).thenReturn(municipioFake);
        when(municipioRepository.save(any(Municipio.class))).thenReturn(municipioFake);
        when(municipioMapper.converter(municipioFake)).thenReturn(responseDTO);

        assertDoesNotThrow(() -> municipioService.salvar(requestDTO));

        MunicipioResponseDTO resultado = municipioService.salvar(requestDTO);
        assertNotNull(resultado);
        assertEquals("Rio Branco", resultado.nome()); // Acessando o valor no record

        verify(municipioRepository, times(2)).save(municipioFake);
    }

    @Test
    public void deveBuscarTodosOsMunicipios() {
        Municipio municipio1 = new Municipio();
        municipio1.setId(1L);
        municipio1.setNome("Rio Branco");

        Municipio municipio2 = new Municipio();
        municipio2.setId(2L);
        municipio2.setNome("Cruzeiro do Sul");

        List<Municipio> listaMunicipios = new ArrayList<>();
        listaMunicipios.add(municipio1);
        listaMunicipios.add(municipio2);

        MunicipioResponseDTO responseDTO1 = new MunicipioResponseDTO(1L, "Rio Branco");
        MunicipioResponseDTO responseDTO2 = new MunicipioResponseDTO(2L, "Cruzeiro do Sul");

        when(municipioRepository.findAll()).thenReturn(listaMunicipios);
        when(municipioMapper.converter(municipio1)).thenReturn(responseDTO1);
        when(municipioMapper.converter(municipio2)).thenReturn(responseDTO2);

        List<MunicipioResponseDTO> resultado = municipioService.buscarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(municipioRepository, times(1)).findAll();
    }

    @Test
    public void deveEditarMunicipio() {
        MunicipioRequestDTO requestDTO = new MunicipioRequestDTO(1L, "Rio Branco Editado");

        Municipio municipioFake = new Municipio();
        municipioFake.setId(1L);
        municipioFake.setNome("Rio Branco Editado");

        MunicipioResponseDTO responseDTO = new MunicipioResponseDTO(1L, "Rio Branco Editado");

        when(municipioMapper.converter(requestDTO)).thenReturn(municipioFake);
        when(municipioRepository.save(any(Municipio.class))).thenReturn(municipioFake);
        when(municipioMapper.converter(municipioFake)).thenReturn(responseDTO);

        MunicipioResponseDTO resultado = municipioService.editar(requestDTO);

        assertNotNull(resultado);
        assertEquals("Rio Branco Editado", resultado.nome());
        verify(municipioRepository, times(1)).save(municipioFake);
    }

    @Test
    public void deveExcluirMunicipio() {
        Long idParaDeletar = 1L;
        Municipio municipioFake = new Municipio();
        municipioFake.setId(idParaDeletar);

        when(municipioRepository.findById(idParaDeletar)).thenReturn(Optional.of(municipioFake));
        doNothing().when(municipioRepository).deleteById(idParaDeletar);

        assertDoesNotThrow(() -> municipioService.excluir(idParaDeletar));

        verify(municipioRepository, times(1)).findById(idParaDeletar);
        verify(municipioRepository, times(1)).deleteById(idParaDeletar);
    }
}