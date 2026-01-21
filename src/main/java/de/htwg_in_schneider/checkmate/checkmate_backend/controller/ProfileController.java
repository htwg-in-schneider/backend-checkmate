package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;

    public ProfileController(UserRepository userRepository, TutorRepository tutorRepository) {
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        String sub = jwt.getSubject();

        User backendUser = userRepository.findByOauthId(sub)
                .orElseThrow(() -> new RuntimeException("No backend user for oauthId=" + sub));

        // ✅ TutorId: kommt aus tutor-table (nicht aus app_user)
        Long tutorId = tutorRepository.findByOwnerSub(sub)
                .map(Tutor::getId)
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", backendUser.getId());
        result.put("name", backendUser.getName());
        result.put("email", backendUser.getEmail());
        result.put("role", backendUser.getRole() != null ? backendUser.getRole().name() : null);
        result.put("oauthId", backendUser.getOauthId());

        // ✅ neu:
        result.put("tutorId", tutorId);

        // optional debug
        result.put("tokenIssuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null);
        result.put("tokenAudience", jwt.getAudience());

        return ResponseEntity.ok(result);
    }
}