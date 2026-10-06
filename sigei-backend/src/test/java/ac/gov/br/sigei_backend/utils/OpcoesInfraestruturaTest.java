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

    @Test
    void aguaEEnergiaDevemAceitarGrafiasComAcentoEBarra() {
        InfraestruturaEscola infraestrutura = infraestrutura("FUNCIONANDO", "FLUVIAL");
        infraestrutura.setTipoAgua("Rio/Vertente");
        infraestrutura.setTipoEnergia("NÃO");

        OpcoesInfraestrutura.validar(infraestrutura);

        assertEquals("RIO_VERTENTE", infraestrutura.getTipoAgua());
        assertEquals("NAO", infraestrutura.getTipoEnergia());
    }

    @Test
    void deveRecusarAguaEEnergiaForaDasOpcoes() {
        InfraestruturaEscola agua = infraestrutura("FUNCIONANDO", "FLUVIAL");
        agua.setTipoAgua("CHUVA");
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> OpcoesInfraestrutura.validar(agua));
        assertTrue(erro.getMessage().startsWith("Abastecimento de água inválido"));

        // Os sinônimos da planilha valem só na importação
        InfraestruturaEscola energia = infraestrutura("FUNCIONANDO", "FLUVIAL");
        energia.setTipoEnergia("SIM");
        assertThrows(IllegalArgumentException.class, () -> OpcoesInfraestrutura.validar(energia));
    }

    @Test
    void importacaoDeveConverterOsValoresDaPlanilhaDeAguaEEnergia() {
        assertEquals("POCO", OpcoesInfraestrutura.tipoAguaImportado("POÇO"));
        assertEquals("IGARAPE", OpcoesInfraestrutura.tipoAguaImportado("IGARAPÉ"));
        assertEquals("RIO_VERTENTE", OpcoesInfraestrutura.tipoAguaImportado("RIO/VERTENTE"));
        assertEquals("CACIMBA", OpcoesInfraestrutura.tipoAguaImportado("CACIMBA"));
        assertNull(OpcoesInfraestrutura.tipoAguaImportado("DESATIVADA"));

        assertEquals("REDE_PUBLICA", OpcoesInfraestrutura.tipoEnergiaImportado("REDE ELETRICA"));
        assertEquals("OUTRO", OpcoesInfraestrutura.tipoEnergiaImportado("SIM"));
        assertEquals("NAO", OpcoesInfraestrutura.tipoEnergiaImportado("NÃO"));
        assertEquals("SOLAR", OpcoesInfraestrutura.tipoEnergiaImportado("SOLAR"));
        assertNull(OpcoesInfraestrutura.tipoEnergiaImportado("DESATIVADA"));
        assertNull(OpcoesInfraestrutura.tipoEnergiaImportado(null));
    }

    private static InfraestruturaEscola infraestrutura(String situacao, String tipoAcesso) {
        InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
        infraestrutura.setSituacao(situacao);
        infraestrutura.setTipoAcesso(tipoAcesso);
        return infraestrutura;
    }
}
