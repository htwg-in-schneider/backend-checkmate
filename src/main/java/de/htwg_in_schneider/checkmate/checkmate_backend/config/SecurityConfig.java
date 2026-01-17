package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Bei JWT üblich
            .cors(Customizer.withDefaults()) // Nutzt das corsConfigurationSource Bean weiter unten
            .authorizeHttpRequests(auth -> auth
                // -------- PUBLIC (ohne Login) --------
                .requestMatchers(HttpMethod.GET,
                    "/api/tutors",
                    "/api/tutors/*",
                    "/api/category",
                    "/api/tutors/*/available-dates",
                    "/api/tutors/*/available-times"
                ).permitAll()

                // -------- AUTHENTICATED USER --------
                .requestMatchers(
                    "/api/profile",
                    "/api/users/**",
                    "/api/students/**",
                    "/api/matches/**", // Wichtig für Match-Funktionalität
                    "/api/my/**",
                    "/api/bookings",
                    "/api/chat/**"
                ).authenticated()

                // -------- ADMIN / MODIFICATION --------
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/tutors").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/tutors/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/tutors/**").authenticated()

                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
 @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(
      "http://localhost:5173",
      "https://htwg-in-schneider.github.io/frontend-checkmate/"   // <-- HIER deine GitHub Pages Origin rein (ohne /repo)
    ));

    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization","Content-Type","Origin","Accept"));
    config.setExposedHeaders(List.of("Authorization"));

    // ✅ Bei Bearer Token normalerweise false
    config.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}