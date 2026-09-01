package ac.gov.br.sigei_backend.services;

import ac.gov.br.sigei_backend.DTOs.request.AlterarSenhaRequestDTO;
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
    JwtService jwtService;

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

        String token = jwtService.gerarToken(usuario);
        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAlterarSenha(),
                token
        );
    }

    public void alterarSenha(Long usuarioId, AlterarSenhaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual inválida");
        }

        if (!dto.novaSenha().equals(dto.confirmacaoSenha())) {
            throw new IllegalArgumentException("Nova senha e confirmação não coincidem");
        }

        if (passwordEncoder.matches(dto.novaSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Nova senha deve ser diferente da senha atual");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuario.setAlterarSenha(false);
        usuarioRepository.save(usuario);
    }
}
