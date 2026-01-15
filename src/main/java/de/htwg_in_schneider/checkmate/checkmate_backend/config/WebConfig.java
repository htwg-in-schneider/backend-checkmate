package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 1. Erlaube deine lokale Entwicklung und die GitHub-Live-Seite
                .allowedOrigins(
                    "http://localhost:5173", 
                    "https://htwg-in-schneider.github.io"
                )
                // 2. Erlaube die Standard-Methoden inkl. OPTIONS für Preflight-Requests
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 3. WICHTIG: Erlaube den Authorization-Header für Auth0
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);
    }
}