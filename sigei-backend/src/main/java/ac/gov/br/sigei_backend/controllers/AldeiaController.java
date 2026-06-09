package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.services.AldeiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aldeia")
public class AldeiaController {

    @Autowired
    AldeiaService aldeiaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody AldeiaRequestDTO aldeiaDTO){
       AldeiaResponseDTO aldeiaResponseDTO = aldeiaService.salvar(aldeiaDTO);
       return new ResponseEntity<AldeiaResponseDTO>(aldeiaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<AldeiaResponseDTO>> buscar(){
        List<AldeiaResponseDTO> aldeias = aldeiaService.buscarTudo();
        return new ResponseEntity<List<AldeiaResponseDTO>>(aldeias, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<AldeiaResponseDTO> editar(@RequestBody AldeiaRequestDTO aldeiaDTO){
        AldeiaResponseDTO aldeiaResponseDTO = aldeiaService.editar(aldeiaDTO);
        return new ResponseEntity<AldeiaResponseDTO>(aldeiaResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/exluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        aldeiaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
