package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.LoginResponseDTO;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AuthService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO dto) {

        Usuario usuario = usuarioRepository
                .findByEmail(dto.email())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "E-mail ou senha inválidos"
                        )
                );

        if (!usuario.getAtivo()) {
            throw new RuntimeException(
                    "Usuário desativado"
            );
        }

        boolean senhaValida = passwordEncoder.matches(
                dto.senha(),
                usuario.getSenha()
        );

        if (!senhaValida) {
            throw new RuntimeException(
                    "E-mail ou senha inválidos"
            );
        }

        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil()
        );
    }
}