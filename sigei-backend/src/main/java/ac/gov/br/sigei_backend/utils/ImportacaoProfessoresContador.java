package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.dto.response.ImportacaoProfessoresResultadoDTO;

import java.util.ArrayList;
import java.util.List;

public class ImportacaoProfessoresContador {

    private int linhasLidas;
    private int professoresCriados;
    private int professoresAtualizados;
    private int lotacoesCriadas;
    private int lotacoesAtualizadas;
    private int vagasEmAberto;
    private int lotacoesNoNucleo;
    private int linhasIgnoradas;

    private final List<String> erros = new ArrayList<>();

    public void incrementarLinhasLidas() {
        linhasLidas++;
    }

    public void incrementarProfessoresCriados() {
        professoresCriados++;
    }

    public void incrementarProfessoresAtualizados() {
        professoresAtualizados++;
    }

    public void incrementarLotacoesCriadas() {
        lotacoesCriadas++;
    }

    public void incrementarLotacoesAtualizadas() {
        lotacoesAtualizadas++;
    }

    public void incrementarVagasEmAberto() {
        vagasEmAberto++;
    }

    public void incrementarLotacoesNoNucleo() {
        lotacoesNoNucleo++;
    }

    public void adicionarErro(int numeroLinha, String mensagem) {
        linhasIgnoradas++;
        erros.add("Linha " + numeroLinha + ": " + mensagem);
    }

    public ImportacaoProfessoresResultadoDTO gerarResultado() {
        return new ImportacaoProfessoresResultadoDTO(
                linhasLidas,
                professoresCriados,
                professoresAtualizados,
                lotacoesCriadas,
                lotacoesAtualizadas,
                vagasEmAberto,
                lotacoesNoNucleo,
                linhasIgnoradas,
                List.copyOf(erros)
        );
    }
}
