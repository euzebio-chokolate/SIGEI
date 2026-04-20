package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.EscolaDTO;
import ac.gov.br.sigei_backend.services.EscolaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/escola")
public class EscolaController {
    @Autowired
    private EscolaService escolaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@RequestBody EscolaDTO escolaDTO) {
        try {
            escolaService.salvar(escolaDTO);

        } catch (DataIntegrityViolationException erro) {
            return new ResponseEntity<String>("Coordenadas geagráficas ja cadastradas!", HttpStatus.CONFLICT);
        }
        return new ResponseEntity(escolaDTO, HttpStatus.CREATED);
    }



}
