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
      // falls ihr Cookies NICHT nutzt (bei JWT üblich):
      .csrf(csrf -> csrf.disable())

      // CORS: wenn ihr schon @CrossOrigin nutzt, reicht oft das hier.
      // Wenn ihr Probleme bekommt, sag Bescheid – dann machen wir ein richtiges CorsConfigurationSource Bean.
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
        // Profil / eigene Buchungen / Checkout-POST / Chat
        .requestMatchers(
          "/api/profile",
          "/api/my/**",
          "/api/bookings",
          "/api/chat/**"
        ).authenticated()

        // -------- ADMIN --------
        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

        // Tutor anlegen / ändern / löschen -> Admin
        .requestMatchers(HttpMethod.POST, "/api/tutors").hasAuthority("ROLE_ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/tutors/**").hasAuthority("ROLE_ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/tutors/**").hasAuthority("ROLE_ADMIN")

        // alles andere:
        .anyRequest().permitAll()
      )

      // JWT Resource Server (Auth0)
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }
}