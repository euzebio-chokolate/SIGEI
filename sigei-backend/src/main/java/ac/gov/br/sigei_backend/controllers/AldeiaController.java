package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.services.AldeiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aldeia")
public class AldeiaController {

    @Autowired
    AldeiaService aldeiaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody AldeiaRequestDTO aldeiaRequestDTO){
        try {
            AldeiaResponseDTO aldeiaResponseDTO = aldeiaService.salvar(aldeiaRequestDTO);
            return new ResponseEntity<AldeiaResponseDTO>(aldeiaResponseDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException erro){
            return new ResponseEntity<String>("Esta aldeia ja foi registrada", HttpStatus.CONFLICT);
        }
    }

}
