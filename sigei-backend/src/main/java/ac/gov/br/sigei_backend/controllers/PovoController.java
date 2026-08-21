package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.services.PovoService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/povo")
public class PovoController {
    @Autowired
    PovoService povoService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody PovoRequestDTO povoDTO){
        PovoResponseDTO povoResponseDTO = povoService.salvar(povoDTO);
        return new ResponseEntity<PovoResponseDTO>(povoResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity buscarTudo(){
        List<PovoResponseDTO> povos = povoService.buscarTudo();
        return new ResponseEntity<List<PovoResponseDTO>>(povos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PovoResponseDTO> buscarPorId(@Parameter(description = "ID de povo", example = "1") @PathVariable Long id){
        PovoResponseDTO povoResponseDTO = povoService.buscarPorId(id);
        return new ResponseEntity<>(povoResponseDTO, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<PovoResponseDTO> editar(@RequestBody PovoRequestDTO povoDTO){
        PovoResponseDTO povoResponseDTO = povoService.editar(povoDTO);
        return new ResponseEntity<PovoResponseDTO>(povoResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        povoService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
