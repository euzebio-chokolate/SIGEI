package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.service.InfraestruturaEscolaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping("/buscarPorAno")
    public ResponseEntity<List<InfraestruturaEscolaResponseDTO>> buscarPorAno(@RequestParam Integer ano) {
        List<InfraestruturaEscolaResponseDTO> infraestruturas = infraestruturaService.buscarPorAno(ano);
        return new ResponseEntity<>(infraestruturas, HttpStatus.OK);
    }

    @PutMapping("/editar")
    public ResponseEntity<InfraestruturaEscolaResponseDTO> editar(@RequestBody InfraestruturaEscolaRequestDTO infraDTO){
        InfraestruturaEscolaResponseDTO infraestruturaEscolaResponseDTO = infraestruturaService.editar(infraDTO);
        return new ResponseEntity<InfraestruturaEscolaResponseDTO>(infraestruturaEscolaResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<InfraestruturaEscolaResponseDTO> adicionarFoto(
            @PathVariable Long id,
            @RequestParam("foto") MultipartFile foto
    ) throws IOException {

        InfraestruturaEscolaResponseDTO infraestrutura =
                infraestruturaService.adicionarFoto(id, foto);

        return new ResponseEntity<>(
                infraestrutura,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        infraestruturaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
