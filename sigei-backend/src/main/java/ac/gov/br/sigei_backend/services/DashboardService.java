package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.response.dashboard.DashboardInfraestruturaDTO;
import ac.gov.br.sigei_backend.DTOs.response.dashboard.DashboardItemDTO;
import ac.gov.br.sigei_backend.DTOs.response.dashboard.DashboardResponseDTO;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.DashboardRepository;
import ac.gov.br.sigei_backend.repository.projection.DashboardInfraestruturaProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final AnoReferenciaRepository anoReferenciaRepository;

    public DashboardService(
            DashboardRepository dashboardRepository,
            AnoReferenciaRepository anoReferenciaRepository
    ) {
        this.dashboardRepository = dashboardRepository;
        this.anoReferenciaRepository = anoReferenciaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO buscar(Integer ano) {
        Integer anoSelecionado = resolverAno(ano);

        return new DashboardResponseDTO(
                anoSelecionado,
                dashboardRepository.buscarResumo(anoSelecionado),
                dashboardRepository.buscarEvolucaoMatriculas(),
                dashboardRepository.buscarEvolucaoEscolas(),
                dashboardRepository.buscarMatriculasPorNivel(anoSelecionado),
                montarInfraestrutura(dashboardRepository.buscarInfraestrutura(anoSelecionado)),
                dashboardRepository.buscarEscolasPorMunicipio(anoSelecionado),
                normalizarSituacoes(dashboardRepository.buscarSituacaoEscolas(anoSelecionado))
        );
    }

    private Integer resolverAno(Integer ano) {
        if (ano != null) {
            return anoReferenciaRepository.findByAno(ano)
                    .map(AnoReferencia::getAno)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Ano de referência não encontrado: " + ano));
        }

        return anoReferenciaRepository.findTopByOrderByAnoDesc()
                .map(AnoReferencia::getAno)
                .orElseThrow(() -> new NoSuchElementException(
                        "Nenhum ano de referência cadastrado"));
    }

    private DashboardInfraestruturaDTO montarInfraestrutura(
            DashboardInfraestruturaProjection resultado
    ) {
        long total = valorLong(resultado.getTotalAvaliado());
        long internet = valorLong(resultado.getComInternet());
        long energia = valorLong(resultado.getComEnergia());
        long predio = valorLong(resultado.getComPredio());

        return new DashboardInfraestruturaDTO(
                total,
                internet,
                percentual(internet, total),
                energia,
                percentual(energia, total),
                predio,
                percentual(predio, total)
        );
    }

    private long valorLong(Long valor) {
        return valor == null ? 0L : valor;
    }

    private double percentual(long quantidade, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((quantidade * 10000.0) / total) / 100.0;
    }

    private List<DashboardItemDTO> normalizarSituacoes(List<DashboardItemDTO> situacoes) {
        Map<String, Long> agrupadas = new LinkedHashMap<>();

        for (DashboardItemDTO item : situacoes) {
            String descricao = item.descricao() == null || item.descricao().isBlank()
                    ? "NÃO INFORMADA"
                    : item.descricao().trim().toUpperCase(Locale.ROOT);
            agrupadas.merge(descricao, item.total(), Long::sum);
        }

        return agrupadas.entrySet().stream()
                .map(item -> new DashboardItemDTO(item.getKey(), item.getValue()))
                .sorted(Comparator.comparing(DashboardItemDTO::total).reversed())
                .toList();
    }
}
