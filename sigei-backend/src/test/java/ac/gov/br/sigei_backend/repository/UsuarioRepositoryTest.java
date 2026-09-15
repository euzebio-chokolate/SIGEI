package ac.gov.br.sigei_backend.repository;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    public void prepararCenario() {
        usuario = new Usuario();
        usuario.setNome("Admin Teste");
        usuario.setEmail("teste@sigei.ac.gov.br");
        usuario.setSenha("123456");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        usuarioRepository.save(usuario);
    }

    @Test
    public void deveSalvarUsuario() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Novo Usuario");
        novoUsuario.setEmail("outro_email@sigei.ac.gov.br");
        novoUsuario.setSenha("123456");
        novoUsuario.setPerfil(PerfilUsuario.USUARIO);
        novoUsuario.setAtivo(true);
        novoUsuario.setAlterarSenha(false);

        usuarioRepository.save(novoUsuario);

        assertNotNull(novoUsuario.getId());
    }

    @Test
    public void deveBuscarUsuarioPorId() {
        Optional<Usuario> resultado = usuarioRepository.findById(usuario.getId());

        assertTrue(resultado.isPresent());
        assertEquals(usuario.getId(), resultado.get().getId());
    }

    @Test
    public void deveBuscarUsuarioPorEmail() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("teste@sigei.ac.gov.br");

        assertTrue(resultado.isPresent());
        assertEquals("teste@sigei.ac.gov.br", resultado.get().getEmail());
    }

    @Test
    public void deveVerificarSeExistePorEmail() {
        assertTrue(usuarioRepository.existsByEmail("teste@sigei.ac.gov.br"));
        assertFalse(usuarioRepository.existsByEmail("inexistente@sigei.ac.gov.br"));
    }
}
