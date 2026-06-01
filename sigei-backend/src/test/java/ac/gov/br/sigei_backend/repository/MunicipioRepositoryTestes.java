package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Municipio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MunicipioRepositoryTestes {
    @Autowired
    MunicipioRepository municipioRepository;

    @Test
    public void deveSalvarMunicipio() {
        // Preparação
        Municipio municipio = new Municipio();
        municipio.setNome("Rio Branco");

        // Ação
        municipioRepository.save(municipio);

        // Verificação: Garantir que o ID foi gerado
        assertNotEquals(null, municipio.getId());

        // Verificação: Buscar no banco e checar se os dados estão corretos
        Optional<Municipio> municipioBuscado = municipioRepository.findById(municipio.getId());
        assertEquals("Rio Branco", municipioBuscado.get().getNome());
    }

    @Test
    public void deveBuscarMunicipioPorNome() {
        // Preparação
        Municipio municipio = new Municipio();
        municipio.setNome("Cruzeiro do Sul");

        municipioRepository.save(municipio);

        // Ação: Utilizar o método customizado do seu repositório
        Optional<Municipio> optionalMunicipio = municipioRepository.findByNome("Cruzeiro do Sul");

        // Verificação: Garantir que o município foi encontrado pelo nome
        assertTrue(optionalMunicipio.isPresent());
        assertNotEquals(null, optionalMunicipio.get());
        assertEquals("Cruzeiro do Sul", optionalMunicipio.get().getNome());
    }
}
