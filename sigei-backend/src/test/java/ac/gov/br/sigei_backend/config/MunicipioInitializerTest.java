package ac.gov.br.sigei_backend.config;

import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MunicipioInitializerTest {

    @Test
    void deveInserirOsVinteEDoisMunicipiosQuandoNaoExistirem() {
        MunicipioRepository repository = mock(MunicipioRepository.class);

        MunicipioInitializer initializer = new MunicipioInitializer(repository);
        initializer.run(new DefaultApplicationArguments());

        verify(repository).saveAll(argThat(municipios -> {
            List<Municipio> lista = (List<Municipio>) municipios;
            return lista.size() == 22
                    && lista.stream().anyMatch(m -> "Rio Branco".equals(m.getNome()))
                    && lista.stream().anyMatch(m -> "Acrelândia".equals(m.getNome()))
                    && lista.stream().anyMatch(m -> "Xapuri".equals(m.getNome()));
        }));
    }

    @Test
    void naoDeveDuplicarMunicipiosJaExistentes() {
        MunicipioRepository repository = mock(MunicipioRepository.class);
        Municipio rioBranco = new Municipio();
        rioBranco.setNome("RIO BRANCO");
        org.mockito.Mockito.when(repository.findAll()).thenReturn(List.of(rioBranco));

        MunicipioInitializer initializer = new MunicipioInitializer(repository);
        initializer.run(new DefaultApplicationArguments());

        verify(repository).saveAll(argThat(municipios -> {
            List<Municipio> lista = (List<Municipio>) municipios;
            return lista.size() == 21
                    && lista.stream().noneMatch(m -> "Rio Branco".equals(m.getNome()));
        }));
    }
}
