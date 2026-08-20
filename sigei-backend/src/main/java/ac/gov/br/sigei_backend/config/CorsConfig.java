package ac.gov.br.sigei_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permite envio de credenciais/cookies
        config.setAllowCredentials(true);

        // Origem do seu Frontend Angular
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://172.16.46.41:4200",
                "http://54.232.189.113:4200", // Removido a /
                "http://172.27.192.1:4200",   // Removido a /
                "http://172.20.208.1:4200"    // Removido a /
        ));

        // Cabeçalhos permitidos na requisição
        config.setAllowedHeaders(Arrays.asList(
                "Origin", "Content-Type", "Accept", "Authorization",
                "X-Requested-With", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Métodos HTTP permitidos (incluindo o OPTIONS para o Preflight)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}