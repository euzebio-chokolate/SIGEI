package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.UsuarioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.UsuarioResponseDTO;
import ac.gov.br.sigei_backend.mappers.UsuarioMapper;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    UsuarioMapper usuarioMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO salvar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(EmailUtils.normalizar(dto.email()))) {
            throw new DataIntegrityViolationException("Já existe um usuário cadastrado com esse e-mail");
        }
        if (dto.senha() == null || dto.senha().isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }

        Usuario usuario = usuarioMapper.converter(dto);

        usuario.setSenha(
                passwordEncoder.encode(dto.senha())
        );

        usuario.setAtivo(
                dto.ativo() == null ? true : dto.ativo()
        );

        usuario.setAlterarSenha(true);

        Usuario usuarioSalvo =
                usuarioRepository.save(usuario);
        return usuarioMapper.converter(usuarioSalvo);
    }

    public List<UsuarioResponseDTO> buscarTudo() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::converter)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        return usuarioMapper.converter(usuario);
    }

    public UsuarioResponseDTO editar(UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        String email = EmailUtils.normalizar(dto.email());
        if (!usuario.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Já existe um usuário cadastrado com esse e-mail");
        }

        usuarioMapper.atualizar(dto, usuario);

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
            usuario.setAlterarSenha(true);
        }

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return usuarioMapper.converter(usuarioSalvo);
    }

    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
