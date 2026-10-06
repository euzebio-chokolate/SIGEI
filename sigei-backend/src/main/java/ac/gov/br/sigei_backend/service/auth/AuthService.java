package ac.gov.br.sigei_backend.service.auth;

import ac.gov.br.sigei_backend.dto.request.AlterarSenhaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LoginResponseDTO;
import ac.gov.br.sigei_backend.exceptions.CredenciaisInvalidasException;
import ac.gov.br.sigei_backend.exceptions.TokenInvalidoException;
import ac.gov.br.sigei_backend.exceptions.UsuarioDesativadoException;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    JwtService jwtService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RefreshTokenService refreshTokenService;

    /** Hash de uma senha aleatória, comparado quando o e-mail não existe. */
    private volatile String hashFicticio;

    /**
     * E-mail inexistente e senha incorreta resultam na mesma resposta (401), e a
     * comparação BCrypt é feita mesmo sem usuário, para que nem a mensagem nem o
     * tempo de resposta revelem quais e-mails estão cadastrados. O estado da conta
     * só é informado (403) a quem acerta a senha.
     */
    public SessaoAutenticada login(LoginRequestDTO dto) {

        Usuario usuario = usuarioRepository
                .findByEmail(dto.email())
                .orElse(null);

        boolean senhaValida = passwordEncoder.matches(
                dto.senha(),
                usuario != null ? usuario.getSenha() : hashFicticio()
        );

        if (usuario == null || !senhaValida) {
            throw new CredenciaisInvalidasException();
        }

        if (!usuario.getAtivo()) {
            throw new UsuarioDesativadoException();
        }

        return gerarSessao(usuario);
    }

    private String hashFicticio() {
        if (hashFicticio == null) {
            hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
        }
        return hashFicticio;
    }

    /** Troca um refresh token válido por um novo par de tokens (rotação). */
    @Transactional(noRollbackFor = TokenInvalidoException.class)
    public SessaoAutenticada renovar(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenInvalidoException("Sessão não encontrada. Entre novamente.");
        }
        Usuario usuario = refreshTokenService.consumir(refreshToken);
        return gerarSessao(usuario);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revogar(refreshToken);
        }
    }

    private SessaoAutenticada gerarSessao(Usuario usuario) {
        LoginResponseDTO dados = new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAlterarSenha()
        );
        return new SessaoAutenticada(dados, jwtService.gerarToken(usuario), refreshTokenService.emitir(usuario));
    }

    @Transactional
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

        // Sessões abertas com a senha anterior deixam de poder ser renovadas
        refreshTokenService.revogarTodos(usuario);
    }
}
