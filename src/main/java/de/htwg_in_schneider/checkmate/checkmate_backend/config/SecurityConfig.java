package de.htwg_in_schneider.checkmate.checkmate_backend.config;

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

        // ✅ Checkout MUSS auth sein (sonst jwt == null / 500)
        .requestMatchers(HttpMethod.POST, "/api/transactions/checkout").authenticated()

        // -------- ADMIN --------
        .requestMatchers("/api/admin/**").authenticated()

        // Tutor anlegen / ändern / löschen (Admin-Check macht ihr im Controller via DB)
        .requestMatchers(HttpMethod.POST, "/api/tutors").authenticated()
        .requestMatchers(HttpMethod.PUT, "/api/tutors/**").authenticated()
        .requestMatchers(HttpMethod.DELETE, "/api/tutors/**").authenticated()

        // alles andere
        .anyRequest().permitAll()
      )

      // JWT Resource Server (Auth0)
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }
}