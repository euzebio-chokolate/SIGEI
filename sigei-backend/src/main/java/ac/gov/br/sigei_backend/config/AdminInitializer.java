package ac.gov.br.sigei_backend.config;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminNome;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${sigei.admin.name}") String adminNome,
            @Value("${sigei.admin.email}") String adminEmail,
            @Value("${sigei.admin.password:}") String adminPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminNome = adminNome;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByEmail(adminEmail)) {
            LOGGER.info("Usuário administrador inicial já existe: {}", adminEmail);
            return;
        }

        boolean generatedPassword = adminPassword.isBlank();
        String temporaryPassword = generatedPassword ? generateTemporaryPassword() : adminPassword;

        Usuario admin = new Usuario();
        admin.setNome(adminNome);
        admin.setEmail(adminEmail);
        admin.setSenha(passwordEncoder.encode(temporaryPassword));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAlterarSenha(true);

        usuarioRepository.save(admin);

        if (generatedPassword) {
            LOGGER.warn("Administrador inicial criado. E-mail: {} | Senha temporária: {}",
                    adminEmail, temporaryPassword);
        } else {
            LOGGER.info("Administrador inicial criado com a senha definida em SIGEI_ADMIN_PASSWORD: {}",
                    adminEmail);
        }
        LOGGER.warn("Altere a senha temporária no primeiro acesso.");
    }

    private String generateTemporaryPassword() {
        return "Adm@" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
