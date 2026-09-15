package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnoReferenciaResponseDTO;
import ac.gov.br.sigei_backend.service.AnoReferenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anos")
public class AnoReferenciaController {

    @Autowired
    AnoReferenciaService anoReferenciaService;

    @PostMapping("/salvar")
    public ResponseEntity<AnoReferenciaResponseDTO> salvar(@Valid @RequestBody AnoReferenciaRequestDTO dto) {
        return new ResponseEntity<>(anoReferenciaService.salvar(dto), HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<AnoReferenciaResponseDTO>> buscarTudo() {
        return ResponseEntity.ok(anoReferenciaService.buscarTudo());
    }

    @PutMapping("/editar")
    public ResponseEntity<AnoReferenciaResponseDTO> editar(@Valid @RequestBody AnoReferenciaRequestDTO dto) {
        return ResponseEntity.ok(anoReferenciaService.editar(dto));
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        anoReferenciaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}