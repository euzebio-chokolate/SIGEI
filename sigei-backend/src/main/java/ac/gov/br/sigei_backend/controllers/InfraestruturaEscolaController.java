package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.services.InfraestruturaEscolaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infraestrutura")
public class InfraestruturaEscolaController {
    @Autowired
    InfraestruturaEscolaService infraestruturaService;

    @PostMapping("/salvar")
    public ResponseEntity salvar(@Valid @RequestBody InfraestruturaEscolaRequestDTO infraDTO){
        InfraestruturaEscolaResponseDTO infraestruturaEscolaResponseDTO = infraestruturaService.salvar(infraDTO);
        return new ResponseEntity<InfraestruturaEscolaResponseDTO>(infraestruturaEscolaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<InfraestruturaEscolaResponseDTO>> buscarTudo(){
        List<InfraestruturaEscolaResponseDTO> infras = infraestruturaService.buscarTudo();
        return new ResponseEntity<List<InfraestruturaEscolaResponseDTO>>(infras, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<InfraestruturaEscolaResponseDTO> editar(@RequestBody InfraestruturaEscolaRequestDTO infraDTO){
        InfraestruturaEscolaResponseDTO infraestruturaEscolaResponseDTO = infraestruturaService.editar(infraDTO);
        return new ResponseEntity<InfraestruturaEscolaResponseDTO>(infraestruturaEscolaResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        infraestruturaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
