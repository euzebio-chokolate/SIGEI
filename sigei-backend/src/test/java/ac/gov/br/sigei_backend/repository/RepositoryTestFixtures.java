package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.*;

final class RepositoryTestFixtures {
    private RepositoryTestFixtures() {}

    static Escola escola(EscolaRepository escolas, AldeiaRepository aldeias,
                         TerraIndigenaRepository terras, MunicipioRepository municipios,
                         PovoRepository povos, String sufixo) {
        Municipio municipio = new Municipio();
        municipio.setNome("Municipio " + sufixo);
        municipios.save(municipio);
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra " + sufixo);
        terra.setMunicipio(municipio);
        terras.save(terra);
        Povo povo = new Povo();
        povo.setNome("Povo " + sufixo);
        povos.save(povo);
        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia " + sufixo);
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeias.save(aldeia);
        Escola escola = new Escola();
        escola.setCodigo("COD-" + sufixo);
        escola.setNome("Escola " + sufixo);
        escola.setTipoUnidade("Polo");
        escola.setAldeia(aldeia);
        return escolas.save(escola);
    }

    static RegistroAnual registro(RegistroAnualRepository registros, AnoReferenciaRepository anos,
                                  Escola escola, int ano, int totalAlunos) {
        AnoReferencia referencia = anos.findByAno(ano).orElseGet(() -> {
            AnoReferencia novo = new AnoReferencia();
            novo.setAno(ano);
            return anos.save(novo);
        });
        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(referencia);
        registro.setRede("Estadual");
        registro.setLocalFuncionamento("Proprio");
        registro.setTotalAlunos(totalAlunos);
        return registros.save(registro);
    }
}
