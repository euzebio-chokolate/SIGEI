package ac.gov.br.sigei_backend.config.initializer;

import ac.gov.br.sigei_backend.dto.response.ImportacaoProfessoresResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.ProfessorRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.service.AtualizacaoAnualService;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoExcelService;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoMatriculasService;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoProfessoresService;
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
    private final AnoReferenciaRepository anoReferenciaRepository;
    private final ProfessorRepository professorRepository;
    private final ImportacaoExcelService importacaoExcelService;
    private final ImportacaoMatriculasService importacaoMatriculasService;
    private final ImportacaoProfessoresService importacaoProfessoresService;
    private final AtualizacaoAnualService atualizacaoAnualService;
    private final boolean habilitado;
    private final Resource planilhaInfraestrutura;
    private final Resource planilhaMatriculas;
    private final Resource planilhaProfessores;
    private final int anoEscolas;
    private final int anoProfessores;

    public DadosIniciaisInitializer(
            RegistroAnualRepository registroAnualRepository,
            AnoReferenciaRepository anoReferenciaRepository,
            ProfessorRepository professorRepository,
            ImportacaoExcelService importacaoExcelService,
            ImportacaoMatriculasService importacaoMatriculasService,
            ImportacaoProfessoresService importacaoProfessoresService,
            AtualizacaoAnualService atualizacaoAnualService,
            @Value("${sigei.dados-iniciais.enabled:true}") boolean habilitado,
            @Value("${sigei.dados-iniciais.infraestrutura}") Resource planilhaInfraestrutura,
            @Value("${sigei.dados-iniciais.matriculas}") Resource planilhaMatriculas,
            @Value("${sigei.dados-iniciais.professores}") Resource planilhaProfessores,
            @Value("${sigei.dados-iniciais.ano-escolas:2025}") int anoEscolas,
            @Value("${sigei.dados-iniciais.ano-professores:2026}") int anoProfessores
    ) {
        this.registroAnualRepository = registroAnualRepository;
        this.anoReferenciaRepository = anoReferenciaRepository;
        this.professorRepository = professorRepository;
        this.importacaoExcelService = importacaoExcelService;
        this.importacaoMatriculasService = importacaoMatriculasService;
        this.importacaoProfessoresService = importacaoProfessoresService;
        this.atualizacaoAnualService = atualizacaoAnualService;
        this.habilitado = habilitado;
        this.planilhaInfraestrutura = planilhaInfraestrutura;
        this.planilhaMatriculas = planilhaMatriculas;
        this.planilhaProfessores = planilhaProfessores;
        this.anoEscolas = anoEscolas;
        this.anoProfessores = anoProfessores;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!habilitado) {
            LOGGER.info("Carga automática dos dados iniciais desabilitada");
            return;
        }

        boolean bancoNovo = registroAnualRepository.count() == 0;

        if (bancoNovo) {
            carregarEscolas();
        } else {
            LOGGER.info("Banco já possui registros anuais; carga automática das escolas ignorada");
        }

        carregarProfessores(bancoNovo);
    }

    private void carregarEscolas() {
        validarPlanilha(planilhaInfraestrutura, "infraestrutura");
        validarPlanilha(planilhaMatriculas, "matrículas");

        LOGGER.info("Banco vazio detectado; iniciando carga automática das planilhas");

        ImportacaoResultadoDTO infraestrutura = importacaoExcelService.importarInfraestrutura(
                new ResourceMultipartFile("arquivo", planilhaInfraestrutura),
                anoEscolas
        );

        LOGGER.info(
                "Importação automática de infraestrutura concluída: {} linha(s), {} erro(s)",
                infraestrutura.linhasLidas(),
                infraestrutura.erros().size()
        );

        ImportacaoMatriculasResultadoDTO matriculas = importacaoMatriculasService.importar(
                new ResourceMultipartFile("arquivo", planilhaMatriculas),
                anoEscolas
        );

        LOGGER.info(
                "Importação automática de matrículas concluída: {} linha(s), {} erro(s)",
                matriculas.linhasLidas(),
                matriculas.erros().size()
        );
    }

    /**
     * Executada enquanto não houver professores cadastrados. O quadro é do ano
     * {@code sigei.dados-iniciais.ano-professores} (padrão 2026): num
     * banco novo, que acabou de receber os dados de 2025, o ano seguinte é iniciado
     * (com a cópia usual dos dados) antes da importação. Num banco em uso, o ano não
     * é iniciado automaticamente; a carga fica para a próxima subida após a abertura.
     * Falhas nesta etapa são registradas no log sem impedir a inicialização.
     */
    private void carregarProfessores(boolean bancoNovo) {
        if (professorRepository.count() > 0) {
            LOGGER.info("Banco já possui professores; carga automática do quadro de professores ignorada");
            return;
        }

        if (!planilhaProfessores.exists() || !planilhaProfessores.isReadable()) {
            LOGGER.warn(
                    "Planilha do quadro de professores não encontrada ou sem permissão de leitura: {}",
                    planilhaProfessores.getDescription()
            );
            return;
        }

        int ano = anoProfessores;

        if (anoReferenciaRepository.findByAno(ano).isEmpty()) {
            boolean anoAnteriorEhOUltimo = anoReferenciaRepository.findTopByOrderByAnoDesc()
                    .map(ultimo -> ultimo.getAno() == ano - 1)
                    .orElse(false);

            if (!bancoNovo || !anoAnteriorEhOUltimo) {
                LOGGER.warn(
                        "Ano {} ainda não iniciado; quadro de professores não importado. " +
                                "Inicie o ano e importe a planilha pela tela de professores.",
                        ano
                );
                return;
            }

            LOGGER.info("Iniciando o ano {} para receber o quadro de professores", ano);
            atualizacaoAnualService.iniciarNovoAno();
        }

        try {
            ImportacaoProfessoresResultadoDTO professores = importacaoProfessoresService.importar(
                    new ResourceMultipartFile("arquivo", planilhaProfessores),
                    anoProfessores
            );

            LOGGER.info(
                    "Importação automática do quadro de professores concluída: {} linha(s), " +
                            "{} professor(es), {} lotação(ões), {} erro(s)",
                    professores.linhasLidas(),
                    professores.professoresCriados(),
                    professores.lotacoesCriadas(),
                    professores.erros().size()
            );

            professores.erros().forEach(erro -> LOGGER.warn("Quadro de professores: {}", erro));

        } catch (RuntimeException e) {
            LOGGER.error("Falha na importação automática do quadro de professores", e);
        }
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
