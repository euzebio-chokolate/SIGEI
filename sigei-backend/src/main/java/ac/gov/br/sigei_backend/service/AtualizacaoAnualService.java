package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
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
            RegistroAnual registroAnterior = registroAnualRepository.findTopByEscolaOrderByAnoReferencia_AnoDesc(escola).orElse(null);

            if (registroAnterior == null) {
                continue;
            }

            RegistroAnual novoRegistro = new RegistroAnual();

            novoRegistro.setEscola(escola);
            novoRegistro.setAnoReferencia(novoAnoSalvo);

            novoRegistro.setRede(
                    registroAnterior.getRede()
            );

            novoRegistro.setLocalFuncionamento(
                    registroAnterior.getLocalFuncionamento()
            );

            novoRegistro.setTotalAlunos(
                    registroAnterior.getTotalAlunos()
            );

            RegistroAnual registroSalvo =
                    registroAnualRepository.save(novoRegistro);

            copiarEquipe(
                    registroAnterior,
                    registroSalvo,
                    novoAnoSalvo
            );

            copiarInfraestrutura(
                    registroAnterior,
                    registroSalvo
            );

            copiarMatriculasNivel(
                    registroAnterior,
                    registroSalvo
            );

            copiarMatriculasSerie(
                    registroAnterior,
                    registroSalvo
            );
        }
    }

    private void copiarEquipe(
            RegistroAnual registroAnterior,
            RegistroAnual novoRegistro,
            AnoReferencia novoAno
    ) {

        EquipeEscola equipeAnterior =
                registroAnterior.getEquipe();

        if (equipeAnterior == null) {
            return;
        }

        EquipeEscola novaEquipe = new EquipeEscola();

        novaEquipe.setRegistroAnual(novoRegistro);

        novaEquipe.setAnoReferencia(novoAno);

        novaEquipe.setTemProfessorResponsavel(
                equipeAnterior.isTemProfessorResponsavel()
        );

        novaEquipe.setTemEquipeGestora(
                equipeAnterior.isTemEquipeGestora()
        );

        novaEquipe.setTemMerendeira(
                equipeAnterior.isTemMerendeira()
        );

        novaEquipe.setTemZeladora(
                equipeAnterior.isTemZeladora()
        );

        novaEquipe.setTemBarqueiro(
                equipeAnterior.isTemBarqueiro()
        );

        novaEquipe.setNecessitaTransporte(
                equipeAnterior.isNecessitaTransporte()
        );

        novoRegistro.setEquipe(novaEquipe);
    }

    private void copiarInfraestrutura(
            RegistroAnual registroAnterior,
            RegistroAnual novoRegistro
    ) {

        InfraestruturaEscola infraestruturaAnterior =
                registroAnterior.getInfraestrutura();


        if (infraestruturaAnterior == null) {
            return;
        }

        InfraestruturaEscola novaInfraestrutura =
                new InfraestruturaEscola();

        novaInfraestrutura.setRegistroAnual(novoRegistro);

        novaInfraestrutura.setAnoConstrucao(
                infraestruturaAnterior.getAnoConstrucao()
        );

        novaInfraestrutura.setNumSalas(
                infraestruturaAnterior.getNumSalas()
        );

        novaInfraestrutura.setLatitude(
                infraestruturaAnterior.getLatitude()
        );

        novaInfraestrutura.setLongitude(
                infraestruturaAnterior.getLongitude()
        );

        novaInfraestrutura.setSituacao(
                infraestruturaAnterior.getSituacao()
        );

        novaInfraestrutura.setTipoAcesso(
                infraestruturaAnterior.getTipoAcesso()
        );

        novaInfraestrutura.setTemPredio(
                infraestruturaAnterior.getTemPredio()
        );

        novaInfraestrutura.setTipoAgua(
                infraestruturaAnterior.getTipoAgua()
        );

        novaInfraestrutura.setTemInternet(
                infraestruturaAnterior.getTemInternet()
        );

        novaInfraestrutura.setTipoEnergia(
                infraestruturaAnterior.getTipoEnergia()
        );

        novaInfraestrutura.setTemRegistroFotografico(
                infraestruturaAnterior.getTemRegistroFotografico()
        );
        // Só a URL é copiada: a imagem fica compartilhada entre os anos, e o
        // fotoPublicId continua apenas no registro que fez o upload, para que
        // uma remoção no Cloudinary não apague a foto de outro ano.
        novaInfraestrutura.setFotoUrl(
                infraestruturaAnterior.getFotoUrl());

        novoRegistro.setInfraestrutura(novaInfraestrutura);
    }

    private void copiarMatriculasNivel(
            RegistroAnual registroAnterior,
            RegistroAnual novoRegistro
    ) {

        if (registroAnterior.getMatriculasNivel() == null) {
            return;
        }

        for (MatriculaNivel matriculaAnterior :
                registroAnterior.getMatriculasNivel()) {

            MatriculaNivel novaMatricula = new MatriculaNivel();

            novaMatricula.setRegistro(novoRegistro);

            novaMatricula.setNivel(
                    matriculaAnterior.getNivel()
            );

            novaMatricula.setQtdMasculino(
                    matriculaAnterior.getQtdMasculino()
            );

            novaMatricula.setQtdFeminino(
                    matriculaAnterior.getQtdFeminino()
            );

            novaMatricula.setTotal(
                    matriculaAnterior.getTotal()
            );

            novoRegistro.getMatriculasNivel()
                    .add(novaMatricula);
        }
    }

    private void copiarMatriculasSerie(
            RegistroAnual registroAnterior,
            RegistroAnual novoRegistro
    ) {

        if (registroAnterior.getMatriculasSerie() == null) {
            return;
        }

        for (MatriculaSerie matriculaAnterior :
                registroAnterior.getMatriculasSerie()) {

            MatriculaSerie novaMatricula =
                    new MatriculaSerie();

            novaMatricula.setRegistro(novoRegistro);

            novaMatricula.setDenominacao(
                    matriculaAnterior.getDenominacao()
            );

            novaMatricula.setEtapaEnsino(
                    matriculaAnterior.getEtapaEnsino()
            );

            novaMatricula.setQtdMasculino(
                    matriculaAnterior.getQtdMasculino()
            );

            novaMatricula.setQtdFeminino(
                    matriculaAnterior.getQtdFeminino()
            );

            novaMatricula.setTotal(
                    matriculaAnterior.getTotal()
            );

            novoRegistro.getMatriculasSerie()
                    .add(novaMatricula);
        }
    }
}