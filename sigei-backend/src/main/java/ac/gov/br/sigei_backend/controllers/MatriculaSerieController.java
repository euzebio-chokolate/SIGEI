package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.MatriculaSerieRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MatriculaSerieResponseDTO;
import ac.gov.br.sigei_backend.services.MatriculaSerieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/MatriculaSerie")
public class MatriculaSerieController {
    @Autowired
    MatriculaSerieService matriculaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody MatriculaSerieRequestDTO matriculaDTO){
        MatriculaSerieResponseDTO matriculaSerieResponseDTO = matriculaService.salvar(matriculaDTO);
        return new ResponseEntity<MatriculaSerieResponseDTO>(matriculaSerieResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity buscarTudo(){
        List<MatriculaSerieResponseDTO> matriculas = matriculaService.buscarTudo();
        return new ResponseEntity<List<MatriculaSerieResponseDTO>>(matriculas, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<MatriculaSerieResponseDTO> editar(@RequestBody MatriculaSerieRequestDTO matriculaDTO){
        MatriculaSerieResponseDTO matriculaSerieResponseDTO = matriculaService.editar(matriculaDTO);
        return new ResponseEntity<MatriculaSerieResponseDTO>(matriculaSerieResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        matriculaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
