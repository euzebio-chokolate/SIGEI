package ac.gov.br.sigei_backend.config.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    JwtFilter jwtFilter;

    @Value("${sigei.cors.allowed-origins:http://localhost:4200}")
    List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Double submit cookie: o token CSRF fica no cookie XSRF-TOKEN (legível pelo Angular)
                // e deve voltar no cabeçalho X-XSRF-TOKEN. Requisições com Bearer não usam cookies
                // de sessão e dispensam a verificação; o login ainda não tem sessão a proteger.
                .csrf(csrf -> csrf
                        .spa()
                        .ignoringRequestMatchers("/auth/login")
                        .ignoringRequestMatchers(SecurityConfig::autenticadaPorBearer))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // sendError encaminha a /error, onde o ErroController responde com ProblemDetail;
                // o 403 padrão (AccessDeniedHandlerImpl) já faz o mesmo
                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                        (request, response, erro) -> response.sendError(HttpStatus.UNAUTHORIZED.value())))
                .authorizeHttpRequests(auth -> auth
                        // Encaminhamento interno para /error: a requisição original já foi
                        // autorizada, mas o JwtFilter não roda nele. Sem esta liberação, um 404
                        // ou 500 de usuário autenticado virava 401 e o frontend encerrava a sessão
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/auth/senha").authenticated()
                        .requestMatchers(HttpMethod.GET, "/infraestruturas", "/infraestruturas/**", "/anos/*/infraestruturas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/escolas/*/historico").hasRole("ADMIN")
                        // Antes da regra geral de leitura: a auditoria mostra autores e e-mails de todo o sistema
                        .requestMatchers(HttpMethod.GET, "/auditoria", "/auditoria/**").hasRole("ADMIN")
                        // Idem: a lista de usuários traz nomes, e-mails e perfis
                        .requestMatchers(HttpMethod.GET, "/usuarios", "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "USUARIO")
                        .anyRequest().hasRole("ADMIN")
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                allowedOrigins.stream()
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toList()
        );
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static boolean autenticadaPorBearer(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return authorization != null && authorization.startsWith("Bearer ");
    }
}
