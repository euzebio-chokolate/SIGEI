package ac.gov.br.sigei_backend.config.initializer;

import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.repository.MunicipioRepository;
import ac.gov.br.sigei_backend.utils.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(1)
public class MunicipioInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MunicipioInitializer.class);

    private static final List<String> MUNICIPIOS_DO_ACRE = List.of(
            "Acrelândia",
            "Assis Brasil",
            "Brasiléia",
            "Bujari",
            "Capixaba",
            "Cruzeiro do Sul",
            "Epitaciolândia",
            "Feijó",
            "Jordão",
            "Mâncio Lima",
            "Manoel Urbano",
            "Marechal Thaumaturgo",
            "Plácido de Castro",
            "Porto Acre",
            "Porto Walter",
            "Rio Branco",
            "Rodrigues Alves",
            "Santa Rosa do Purus",
            "Sena Madureira",
            "Senador Guiomard",
            "Tarauacá",
            "Xapuri"
    );

    private final MunicipioRepository municipioRepository;

    public MunicipioInitializer(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> nomesExistentes = municipioRepository.findAll()
                .stream()
                .map(Municipio::getNome)
                .map(ExcelUtils::normalizar)
                .collect(Collectors.toSet());

        List<Municipio> municipiosAusentes = new ArrayList<>();

        for (String nome : MUNICIPIOS_DO_ACRE) {
            if (nomesExistentes.contains(ExcelUtils.normalizar(nome))) {
                continue;
            }

            Municipio municipio = new Municipio();
            municipio.setNome(nome);
            municipiosAusentes.add(municipio);
        }

        municipioRepository.saveAll(municipiosAusentes);

        LOGGER.info(
                "Carga inicial de municípios do Acre concluída: {} inserido(s), {} já existente(s)",
                municipiosAusentes.size(),
                MUNICIPIOS_DO_ACRE.size() - municipiosAusentes.size()
        );
    }
}
