package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.AlterarSenhaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.LoginResponseDTO;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO usuario =
                authService.login(dto);

        return new ResponseEntity<>(
                usuario,
                HttpStatus.OK
        );
    }

    @PutMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AlterarSenhaRequestDTO dto
    ) {
        authService.alterarSenha(usuario.getId(), dto);
        return ResponseEntity.noContent().build();
    }
}
