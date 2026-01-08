package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Jwt jwt) {
        // Auth0 user id (sub) z.B. "auth0|695e5f..."
        String sub = jwt.getSubject();

        User backendUser = userRepository.findByOauthId(sub)
                .orElseThrow(() -> new RuntimeException("No backend user for oauthId=" + sub));

        return ResponseEntity.ok(Map.of(
                "id", backendUser.getId(),
                "name", backendUser.getName(),
                "email", backendUser.getEmail(),
                "role", backendUser.getRole().name(),
                "oauthId", backendUser.getOauthId(),

                // optional: Debug infos aus dem Token
                "tokenIssuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null,
                "tokenAudience", jwt.getAudience()
        ));
    }
}