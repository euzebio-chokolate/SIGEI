package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.services.EscolaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/escola")
public class EscolaController {
    @Autowired
    EscolaService escolaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody EscolaRequestDTO escolaRequestDTO){
        try {
            EscolaResponseDTO escolaResponseDTO = escolaService.salvar(escolaRequestDTO);
            return new ResponseEntity<EscolaResponseDTO>(escolaResponseDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException erro){
            return new ResponseEntity<String>("Esta Escola já foi resgistrada", HttpStatus.CONFLICT);
        }
    }

    @GetMapping("")
    public ResponseEntity<List<EscolaResponseDTO>> buscarTudo(){
        List<EscolaResponseDTO> escolas = escolaService.buscarTudo();
        return new ResponseEntity<List<EscolaResponseDTO>>(escolas, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<EscolaResponseDTO> editar(@RequestBody EscolaRequestDTO escolaDTO){
        EscolaResponseDTO escolaResponseDTO = escolaService.editar(escolaDTO);
        return new ResponseEntity<EscolaResponseDTO>(escolaResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        escolaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
