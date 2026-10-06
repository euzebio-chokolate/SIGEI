package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.enums.TipoUnidade;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Aplica o tipo de unidade e o vínculo com a escola-sede, que é representado
 * apenas por {@code Escola.escolaSede}. Regras: um ANEXO precisa de uma sede do
 * tipo SEDE, diferente de si mesmo; uma SEDE não tem sede; uma escola com anexos
 * vinculados não pode passar a ser ANEXO.
 */
@Service
public class VinculoSedeService {

    private final EscolaRepository escolaRepository;

    public VinculoSedeService(EscolaRepository escolaRepository) {
        this.escolaRepository = escolaRepository;
    }

    public void vincular(Escola escola, String tipoUnidade, Long escolaSedeId) {
        TipoUnidade tipo = converterTipo(tipoUnidade);

        Escola escolaSede = tipo == TipoUnidade.ANEXO
                ? resolverSede(escola, escolaSedeId)
                : semSede(escolaSedeId);

        escola.setTipoUnidade(tipo.name());
        escola.setEscolaSede(escolaSede);
    }

    private TipoUnidade converterTipo(String tipoUnidade) {
        if (tipoUnidade != null) {
            for (TipoUnidade tipo : TipoUnidade.values()) {
                if (tipo.name().equals(tipoUnidade.trim().toUpperCase(Locale.ROOT))) {
                    return tipo;
                }
            }
        }
        throw new IllegalArgumentException("Tipo de unidade inválido: use SEDE ou ANEXO.");
    }

    private Escola semSede(Long escolaSedeId) {
        if (escolaSedeId != null) {
            throw new IllegalArgumentException("Uma escola do tipo SEDE não pode ter escola sede.");
        }
        return null;
    }

    private Escola resolverSede(Escola escola, Long escolaSedeId) {
        if (escolaSedeId == null) {
            throw new IllegalArgumentException("Uma escola do tipo ANEXO deve informar a escola sede.");
        }
        if (escolaSedeId.equals(escola.getId())) {
            throw new IllegalArgumentException("A escola não pode ser sede de si mesma.");
        }

        Escola escolaSede = escolaRepository.findById(escolaSedeId)
                .orElseThrow(() -> new NoSuchElementException("Escola sede não encontrada"));

        if (!TipoUnidade.SEDE.name().equalsIgnoreCase(escolaSede.getTipoUnidade())) {
            throw new IllegalArgumentException("A escola sede informada não é do tipo SEDE.");
        }
        if (escola.getId() != null && escolaRepository.existsByEscolaSede_Id(escola.getId())) {
            throw new IllegalArgumentException(
                    "A escola possui anexos vinculados e não pode passar a ser do tipo ANEXO.");
        }
        return escolaSede;
    }
}
