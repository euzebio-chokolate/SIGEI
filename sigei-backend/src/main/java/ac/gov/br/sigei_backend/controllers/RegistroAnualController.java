package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.services.RegistroAnualService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registroAnual")
public class RegistroAnualController {
    @Autowired
    RegistroAnualService registroAnualService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody RegistroAnualRequestDTO registroDTO){
        RegistroAnualResponseDTO registroAnualResponseDTO = registroAnualService.salvar(registroDTO);
        return new ResponseEntity<RegistroAnualResponseDTO>(registroAnualResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<RegistroAnualResponseDTO>> buscarTudo(){
        List<RegistroAnualResponseDTO> registroDTOS = registroAnualService.buscarTudo();
        return new ResponseEntity<List<RegistroAnualResponseDTO>>(registroDTOS, HttpStatus.OK);
    }

    @GetMapping("/buscarPorEscolaEAno")
    public ResponseEntity<RegistroAnualResponseDTO> buscarPorEscolaEAno(@RequestParam Long escolaId, @RequestParam Integer ano) {

        RegistroAnualResponseDTO registro = registroAnualService.buscarPorEscolaEAno(escolaId, ano);

        return new ResponseEntity<>(registro, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity editar(@Valid @RequestBody RegistroAnualRequestDTO registroDTO){
        RegistroAnualResponseDTO registroAnualResponseDTO = registroAnualService.editar(registroDTO);
        return new ResponseEntity<RegistroAnualResponseDTO>(registroAnualResponseDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        registroAnualService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
