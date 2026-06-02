package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.services.TerraIndigenaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/terra")
public class TerraIndigenaController {
    @Autowired
    TerraIndigenaService terraIndigenaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody TerraIndigenaRequestDTO terraIndigenaDTO){
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.salvar(terraIndigenaDTO);
        return new ResponseEntity<TerraIndigenaResponseDTO>(terraIndigenaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<TerraIndigenaResponseDTO>> buscar(){
        List<TerraIndigenaResponseDTO> terrasIndigenas = terraIndigenaService.buscarTudo();
        return new ResponseEntity<List<TerraIndigenaResponseDTO>>(terrasIndigenas, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<TerraIndigenaResponseDTO> editar(@RequestBody TerraIndigenaRequestDTO terraIndigenaDTO){
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.editar(terraIndigenaDTO);
        return new ResponseEntity<TerraIndigenaResponseDTO>(terraIndigenaResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        terraIndigenaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
