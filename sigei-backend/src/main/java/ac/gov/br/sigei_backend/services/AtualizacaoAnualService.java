package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AtualizacaoAnualService {

    @Autowired
    AnoReferenciaRepository anoReferenciaRepository;

    @Autowired
    EscolaRepository escolaRepository;

    @Autowired
    RegistroAnualRepository registroAnualRepository;

    @Transactional
    public void iniciarNovoAno() {

        AnoReferencia ultimoAno = anoReferenciaRepository
                .findTopByOrderByAnoDesc()
                .orElseThrow(() -> new NoSuchElementException(
                        "Nenhum ano de referência cadastrado"
                ));

        Integer novoAno = ultimoAno.getAno() + 1;

        if (anoReferenciaRepository.findByAno(novoAno).isPresent()) {
            throw new IllegalArgumentException(
                    "O ano " + novoAno + " já foi iniciado"
            );
        }

        AnoReferencia anoReferencia = new AnoReferencia();
        anoReferencia.setAno(novoAno);

        AnoReferencia novoAnoSalvo =
                anoReferenciaRepository.save(anoReferencia);

        List<Escola> escolas = escolaRepository.findAll();

        for (Escola escola : escolas) {

            RegistroAnual registro = new RegistroAnual();

            registro.setEscola(escola);
            registro.setAnoReferencia(novoAnoSalvo);

            registroAnualRepository.save(registro);
        }
    }
}