package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.EscolaDTO;
import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.services.EscolaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/escolas")
public class EscolaController {
    @Autowired
    private EscolaService escolaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody EscolaRequestDTO escolaRequestDTO) {
        try{
            EscolaResponseDTO escolaResponseDTO = escolaService.salvar(escolaRequestDTO);
            return new ResponseEntity<EscolaResponseDTO>(escolaResponseDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException erro) {
            return new ResponseEntity<String>("Coordenadas já pertence á uma escola!",HttpStatus.CONFLICT);
        }
    }

    @GetMapping
    public ResponseEntity buscarTodas(){
        return new ResponseEntity(escolaService.buscarTodas(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity buscarPorId(@PathVariable Long id) {
        try{
            Escola escola = escolaService.buscarPorId(id);
            return new ResponseEntity<Escola>(escola, HttpStatus.OK);
        } catch (IllegalArgumentException erro) {
            return new ResponseEntity<String>(erro.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Escola> excluir(@PathVariable Long id) {
        try{
            escolaService.excluir(id);
        } catch (IllegalArgumentException erro) {
            return new ResponseEntity<Escola>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Escola>(HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Escola> editar(@RequestBody EscolaDTO escolaDTO) {
        escolaService.editar(escolaDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
