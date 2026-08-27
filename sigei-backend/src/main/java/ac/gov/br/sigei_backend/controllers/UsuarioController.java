package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.UsuarioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.UsuarioResponseDTO;
import ac.gov.br.sigei_backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @PostMapping("/salvar")
    public ResponseEntity<UsuarioResponseDTO> salvar(
            @Valid @RequestBody UsuarioRequestDTO dto) {

        UsuarioResponseDTO usuario =
                usuarioService.salvar(dto);

        return new ResponseEntity<>(
                usuario,
                HttpStatus.CREATED
        );
    }

    @GetMapping("")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarTudo() {

        List<UsuarioResponseDTO> usuarios =
                usuarioService.buscarTudo();

        return new ResponseEntity<>(
                usuarios,
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable Long id) {

        UsuarioResponseDTO usuario =
                usuarioService.buscarPorId(id);

        return new ResponseEntity<>(
                usuario,
                HttpStatus.OK
        );
    }

    @PutMapping("/editar")
    public ResponseEntity<UsuarioResponseDTO> editar(
            @Valid @RequestBody UsuarioRequestDTO dto) {

        UsuarioResponseDTO usuario =
                usuarioService.editar(dto);

        return new ResponseEntity<>(
                usuario,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {

        usuarioService.excluir(id);

        return new ResponseEntity<>(
                HttpStatus.OK
        );
    }
}