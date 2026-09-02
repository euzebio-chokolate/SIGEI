package ac.gov.br.sigei_backend.config;

import ac.gov.br.sigei_backend.DTOs.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.DTOs.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.services.ImportacaoExcelService;
import ac.gov.br.sigei_backend.services.ImportacaoMatriculasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Order(2)
public class DadosIniciaisInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DadosIniciaisInitializer.class);

    private final RegistroAnualRepository registroAnualRepository;
    private final ImportacaoExcelService importacaoExcelService;
    private final ImportacaoMatriculasService importacaoMatriculasService;
    private final boolean habilitado;
    private final Resource planilhaInfraestrutura;
    private final Resource planilhaMatriculas;

    public DadosIniciaisInitializer(
            RegistroAnualRepository registroAnualRepository,
            ImportacaoExcelService importacaoExcelService,
            ImportacaoMatriculasService importacaoMatriculasService,
            @Value("${sigei.dados-iniciais.enabled:true}") boolean habilitado,
            @Value("${sigei.dados-iniciais.infraestrutura}") Resource planilhaInfraestrutura,
            @Value("${sigei.dados-iniciais.matriculas}") Resource planilhaMatriculas
    ) {
        this.registroAnualRepository = registroAnualRepository;
        this.importacaoExcelService = importacaoExcelService;
        this.importacaoMatriculasService = importacaoMatriculasService;
        this.habilitado = habilitado;
        this.planilhaInfraestrutura = planilhaInfraestrutura;
        this.planilhaMatriculas = planilhaMatriculas;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!habilitado) {
            LOGGER.info("Carga automática dos dados iniciais desabilitada");
            return;
        }

        if (registroAnualRepository.count() > 0) {
            LOGGER.info("Banco já possui registros anuais; carga automática ignorada");
            return;
        }

        validarPlanilha(planilhaInfraestrutura, "infraestrutura");
        validarPlanilha(planilhaMatriculas, "matrículas");

        LOGGER.info("Banco vazio detectado; iniciando carga automática das planilhas");

        ImportacaoResultadoDTO infraestrutura = importacaoExcelService.importarInfraestrutura(
                new ResourceMultipartFile("arquivo", planilhaInfraestrutura)
        );

        LOGGER.info(
                "Importação automática de infraestrutura concluída: {} linha(s), {} erro(s)",
                infraestrutura.linhasLidas(),
                infraestrutura.erros().size()
        );

        ImportacaoMatriculasResultadoDTO matriculas = importacaoMatriculasService.importar(
                new ResourceMultipartFile("arquivo", planilhaMatriculas)
        );

        LOGGER.info(
                "Importação automática de matrículas concluída: {} linha(s), {} erro(s)",
                matriculas.linhasLidas(),
                matriculas.erros().size()
        );
    }

    private void validarPlanilha(Resource planilha, String tipo) {
        if (!planilha.exists() || !planilha.isReadable()) {
            throw new IllegalStateException(
                    "Planilha de " + tipo + " não encontrada ou sem permissão de leitura: " +
                            planilha.getDescription()
            );
        }
    }

    private static final class ResourceMultipartFile implements MultipartFile {

        private static final String XLSX_CONTENT_TYPE =
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        private final String name;
        private final Resource resource;

        private ResourceMultipartFile(String name, Resource resource) {
            this.name = name;
            this.resource = resource;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return resource.getFilename();
        }

        @Override
        public String getContentType() {
            return XLSX_CONTENT_TYPE;
        }

        @Override
        public boolean isEmpty() {
            return getSize() == 0;
        }

        @Override
        public long getSize() {
            try {
                return resource.contentLength();
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possível obter o tamanho da planilha", e);
            }
        }

        @Override
        public byte[] getBytes() throws IOException {
            try (InputStream inputStream = getInputStream()) {
                return inputStream.readAllBytes();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return resource.getInputStream();
        }

        @Override
        public void transferTo(File destino) throws IOException {
            transferTo(destino.toPath());
        }

        @Override
        public void transferTo(Path destino) throws IOException {
            try (InputStream inputStream = getInputStream()) {
                Files.copy(inputStream, destino);
            }
        }
    }
}
