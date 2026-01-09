package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Value("${spring.security.oauth2.resourceserver.jwt.audience}")
    private String audience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> {})
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth

            // Profile braucht Login
            .requestMatchers("/api/profile").authenticated()

            // Öffentliche READ-Zugriffe
            .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

            .requestMatchers(HttpMethod.POST, "/api/tutors", "/api/tutors/*").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/tutors/*").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/tutors/*").authenticated()
        
            .requestMatchers("/api/profile").authenticated()
            .anyRequest().permitAll()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.decoder(jwtDecoder()))
        );

    return http.build();
}

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> withAudience = jwt -> {
            if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error(
                    "invalid_token",
                    "Missing or invalid audience. Expected: " + audience,
                    null
            );
            return OAuth2TokenValidatorResult.failure(err);
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Frontend Dev Server
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // Falls ihr Cookies/Sessions nutzt (für JWT meist nicht nötig, aber schadet nicht):
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}