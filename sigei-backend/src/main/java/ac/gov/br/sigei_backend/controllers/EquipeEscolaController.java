package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.services.EquipeEscolaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipes")
public class EquipeEscolaController {

    @Autowired
    EquipeEscolaService equipeEscolaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody EquipeEscolaRequestDTO equipeDTO){
        EquipeEscolaResponseDTO equipeEscolaResponseDTO = equipeEscolaService.salvar(equipeDTO);
        return new ResponseEntity<EquipeEscolaResponseDTO>(equipeEscolaResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/editar")
    public ResponseEntity<EquipeEscolaResponseDTO> editar(@RequestBody EquipeEscolaRequestDTO equipeDTO){
        EquipeEscolaResponseDTO equipeEscolaResponseDTO = equipeEscolaService.editar(equipeDTO);
        return new ResponseEntity<EquipeEscolaResponseDTO>(equipeEscolaResponseDTO, HttpStatus.OK);
    }

}
