package de.htwg_in_schneider.checkmate.checkmate_backend.config;
import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Deaktiviert für JWT-Nutzung
                .cors(withDefaults())        // Nutzt deine WebConfig-Einstellungen
                .authorizeHttpRequests((authorize) -> authorize
                        // 1. Profil & Buchungen: Muss nur eingeloggt sein
                        .requestMatchers("/api/profile", "/api/profile/**").authenticated()
                        .requestMatchers("/api/bookings", "/api/bookings/**").authenticated()
                        .requestMatchers("/api/my/**").authenticated()

                        // 2. Tutoren ändern/erstellen: Erstmal nur "authenticated" zum Testen
                        // Später kannst du hier wieder .hasRole("ADMIN") einfügen
                        .requestMatchers(HttpMethod.POST, "/api/tutors", "/api/tutors/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tutors/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/tutors/**").authenticated()

                        // 3. Öffentliche Daten: Jeder darf gucken
                        .requestMatchers(HttpMethod.GET, "/api/tutors", "/api/tutors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/category/**").permitAll()

                        // 4. Alles andere absichern oder erlauben
                        .requestMatchers("/api/**").permitAll() 
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults()))
                .build();
    }
}