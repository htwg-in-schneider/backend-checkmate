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
// ✅ Admin-Endpunkte brauchen Login, Admin-Check machen wir im Controller via DB
.requestMatchers("/api/admin/**").authenticated()

// Tutor anlegen / ändern / löschen -> Admin (auch hier: besser DB-check im Controller)
.requestMatchers(HttpMethod.POST, "/api/tutors").authenticated()
.requestMatchers(HttpMethod.POST, "/api/students/**").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/tutors/**").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/students/**").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/students/**").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/students/**").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/tutors/**").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/matches/**").authenticated()

        // alles andere:
        .anyRequest().permitAll()
      )

      // JWT Resource Server (Auth0)
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }
}