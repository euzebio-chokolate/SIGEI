package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.services.AtualizacaoAnualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atualizacao-anual")
public class AtualizacaoAnualController {

    @Autowired
    AtualizacaoAnualService atualizacaoAnualService;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarNovoAno() {

        atualizacaoAnualService.iniciarNovoAno();

        return new ResponseEntity<>(
                "Novo ano iniciado com sucesso",
                HttpStatus.CREATED
        );
    }
}