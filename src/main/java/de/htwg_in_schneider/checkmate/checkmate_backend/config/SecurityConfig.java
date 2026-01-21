package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
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
                    "/api/my/**",
                    "/api/bookings",
                    "/api/chat/**"
                ).authenticated()

                // -------- AUTHENTICATED (Schreib-Operationen) --------
                // Rollen-/Admin-Check macht ihr (falls nötig) im Controller via DB
                .requestMatchers(HttpMethod.POST, "/api/tutors").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/tutors/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/tutors/**").authenticated()

                .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()

                .requestMatchers(HttpMethod.PUT, "/api/students/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/students/**").authenticated()

                .requestMatchers(HttpMethod.DELETE, "/api/matches/**").authenticated()

                // ✅ Checkout MUSS auth sein (sonst jwt == null / 500)
                .requestMatchers(HttpMethod.POST, "/api/transactions/checkout").authenticated()

                // -------- ADMIN (aktuell: nur authenticated) --------
                .requestMatchers("/api/admin/**").authenticated()

                // -------- OFFERS --------
.requestMatchers(HttpMethod.GET, "/api/offers").permitAll()
.requestMatchers(HttpMethod.GET, "/api/offers/mine").authenticated()
.requestMatchers(HttpMethod.POST, "/api/offers").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/offers/**").authenticated()



                // -------- FALLBACK --------
                .anyRequest().permitAll()
            )
            // JWT Resource Server (Auth0)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}