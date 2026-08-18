package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.response.ImportacaoMatriculasResultadoDTO;

import java.util.ArrayList;
import java.util.List;

public class ImportacaoMatriculasContador {

    private int linhasLidas;
    private int escolasEncontradas;
    private int registrosAnuaisEncontrados;
    private int niveisInseridos;
    private int seriesInseridas;
    private int registrosAnuaisAtualizados;
    private int linhasIgnoradas;

    private final List<String> erros =
            new ArrayList<>();

    public void incrementarLinhasLidas() {
        linhasLidas++;
    }

    public void incrementarEscolasEncontradas() {
        escolasEncontradas++;
    }

    public void incrementarRegistrosAnuaisEncontrados() {
        registrosAnuaisEncontrados++;
    }

    public void adicionarNiveisInseridos(
            int quantidade
    ) {
        niveisInseridos += quantidade;
    }

    public void adicionarSeriesInseridas(
            int quantidade
    ) {
        seriesInseridas += quantidade;
    }

    public void incrementarRegistrosAnuaisAtualizados() {
        registrosAnuaisAtualizados++;
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

    public ImportacaoMatriculasResultadoDTO
    gerarResultado() {

        return new ImportacaoMatriculasResultadoDTO(
                linhasLidas,
                escolasEncontradas,
                registrosAnuaisEncontrados,
                niveisInseridos,
                seriesInseridas,
                registrosAnuaisAtualizados,
                linhasIgnoradas,
                List.copyOf(erros)
        );
    }
}