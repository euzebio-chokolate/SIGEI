package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.ImportacaoResultadoDTO;

import java.util.ArrayList;
import java.util.List;

public class ImportacaoContador {

    private int linhasLidas;
    private int municipiosCriados;
    private int terrasIndigenasCriadas;
    private int povosCriados;
    private int aldeiasCriadas;
    private int escolasCriadas;
    private int registrosAnuaisCriados;
    private int infraestruturasCriadas;
    private int equipesCriadas;
    private int registrosAtualizados;
    private int linhasIgnoradas;

    private final List<String> erros = new ArrayList<>();

    public void incrementarLinhasLidas() {
        linhasLidas++;
    }

    public void incrementarMunicipiosCriados() {
        municipiosCriados++;
    }

    public void incrementarTerrasIndigenasCriadas() {
        terrasIndigenasCriadas++;
    }

    public void incrementarPovosCriados() {
        povosCriados++;
    }

    public void incrementarAldeiasCriadas() {
        aldeiasCriadas++;
    }

    public void incrementarEscolasCriadas() {
        escolasCriadas++;
    }

    public void incrementarRegistrosAnuaisCriados() {
        registrosAnuaisCriados++;
    }

    public void incrementarInfraestruturasCriadas() {
        infraestruturasCriadas++;
    }

    public void incrementarEquipesCriadas() {
        equipesCriadas++;
    }

    public void incrementarRegistrosAtualizados() {
        registrosAtualizados++;
    }

    public void incrementarLinhasIgnoradas() {
        linhasIgnoradas++;
    }

    public void adicionarErro(
            int numeroLinha,
            String mensagem
    ) {
        linhasIgnoradas++;

        erros.add(
                "Linha " +
                        numeroLinha +
                        ": " +
                        mensagem
        );
    }

    public ImportacaoResultadoDTO gerarResultado() {
        return new ImportacaoResultadoDTO(
                linhasLidas,
                municipiosCriados,
                terrasIndigenasCriadas,
                povosCriados,
                aldeiasCriadas,
                escolasCriadas,
                registrosAnuaisCriados,
                infraestruturasCriadas,
                equipesCriadas,
                registrosAtualizados,
                linhasIgnoradas,
                List.copyOf(erros)
        );
    }
}