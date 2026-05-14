package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.services.TerraIndigenaService;
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
@RequestMapping("/terra")
public class TerraIndigenaController {
    @Autowired
    TerraIndigenaService terraIndigenaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody TerraIndigenaRequestDTO terraIndigenaRequestDTO){
        try {
            TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.salvar(terraIndigenaRequestDTO);
            return new ResponseEntity<TerraIndigenaResponseDTO>(terraIndigenaResponseDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException erro) {
            return new ResponseEntity<String>("Esta terra indígena ja foi registrada", HttpStatus.CONFLICT);
        }
    }
}
