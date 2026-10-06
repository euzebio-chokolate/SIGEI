package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpcoesInfraestruturaTest {

    @Test
    void deveGravarOpcoesValidasEmMaiusculasSemAcentos() {
        InfraestruturaEscola infraestrutura = infraestrutura(" paralisada ", "Fluvial");

        OpcoesInfraestrutura.validar(infraestrutura);

        assertEquals("PARALISADA", infraestrutura.getSituacao());
        assertEquals("FLUVIAL", infraestrutura.getTipoAcesso());
    }

    @Test
    void valoresVaziosDevemSerGravadosComoNaoInformados() {
        InfraestruturaEscola infraestrutura = infraestrutura("", null);

        OpcoesInfraestrutura.validar(infraestrutura);

        assertNull(infraestrutura.getSituacao());
        assertNull(infraestrutura.getTipoAcesso());
    }

    @Test
    void deveRecusarValoresForaDasOpcoes() {
        IllegalArgumentException situacao = assertThrows(IllegalArgumentException.class,
                () -> OpcoesInfraestrutura.validar(infraestrutura("EXTINTA", "FLUVIAL")));
        assertEquals("Situação inválida: use FUNCIONANDO, PARALISADA, DESATIVADA.", situacao.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> OpcoesInfraestrutura.validar(infraestrutura("FUNCIONANDO", "AEREO")));
    }

    @Test
    void importacaoDeveConverterValoresForaDasOpcoesEmNaoInformados() {
        assertEquals("FUNCIONANDO", OpcoesInfraestrutura.situacaoImportada("FUNCIONANDO"));
        assertEquals("TERRESTRE", OpcoesInfraestrutura.tipoAcessoImportado("terrestre"));
        // As escolas paralisadas da planilha de 2025 têm "DESATIVADA" na coluna de acesso
        assertNull(OpcoesInfraestrutura.tipoAcessoImportado("DESATIVADA"));
        assertNull(OpcoesInfraestrutura.situacaoImportada("SITUAÇÃO"));
    }

    private static InfraestruturaEscola infraestrutura(String situacao, String tipoAcesso) {
        InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
        infraestrutura.setSituacao(situacao);
        infraestrutura.setTipoAcesso(tipoAcesso);
        return infraestrutura;
    }
}
