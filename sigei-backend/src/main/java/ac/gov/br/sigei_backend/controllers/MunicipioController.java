package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.services.MunicipioService;
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
@RequestMapping("/municipio")
public class MunicipioController {

    @Autowired
    MunicipioService municipioService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody MunicipioRequestDTO municipioRequestDTO){
        try {
            MunicipioResponseDTO municipioResponseDTO = municipioService.salvar(municipioRequestDTO);
            return new ResponseEntity<MunicipioResponseDTO>(municipioResponseDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException erro){
            return new ResponseEntity<String>("Este município já está registrado", HttpStatus.CONFLICT);
        }
    }
}
