package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.MatriculaNivelRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MatriculaNivelResponseDTO;
import ac.gov.br.sigei_backend.services.MatriculaNivelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculaNivel")
public class MatriculaNivelController {
    @Autowired
    MatriculaNivelService matriculaNivelService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody MatriculaNivelRequestDTO matriculaNivelDTO){
        MatriculaNivelResponseDTO matriculaNivelResponseDTO = matriculaNivelService.salvar(matriculaNivelDTO);
        return new ResponseEntity<MatriculaNivelResponseDTO>(matriculaNivelResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<MatriculaNivelResponseDTO>> buscarTudo(){
        List<MatriculaNivelResponseDTO> matriculaNivelDTOS = matriculaNivelService.buscarTudo();
        return new ResponseEntity<List<MatriculaNivelResponseDTO>>(matriculaNivelDTOS, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity editar(@RequestBody MatriculaNivelRequestDTO matriculaNivelDTO){
        MatriculaNivelResponseDTO matriculaNivelResponseDTO = matriculaNivelService.editar(matriculaNivelDTO);
        return new ResponseEntity<MatriculaNivelResponseDTO>(matriculaNivelResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        matriculaNivelService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
