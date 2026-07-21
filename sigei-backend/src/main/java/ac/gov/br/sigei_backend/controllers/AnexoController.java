package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.AnexoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AnexoResponseDTO;
import ac.gov.br.sigei_backend.services.AnexoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anexo")
public class AnexoController {

    @Autowired
    AnexoService anexoService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody AnexoRequestDTO anexoDTO){
        AnexoResponseDTO anexoResponseDTO = anexoService.salvar(anexoDTO);
        return new ResponseEntity<AnexoResponseDTO>(anexoResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity buscarTudo(){
        List<AnexoResponseDTO> anexos = anexoService.buscarTudo();
        return new ResponseEntity<List<AnexoResponseDTO>>(anexos, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<AnexoResponseDTO> editar(@RequestBody AnexoRequestDTO anexoDTO){
        AnexoResponseDTO anexoResponseDTO = anexoService.editar(anexoDTO);
        return new ResponseEntity<AnexoResponseDTO>(anexoResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<AnexoResponseDTO> excluir(@PathVariable Long id){
        anexoService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
