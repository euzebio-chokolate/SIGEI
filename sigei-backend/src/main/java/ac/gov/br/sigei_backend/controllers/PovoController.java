package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.services.PovoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
