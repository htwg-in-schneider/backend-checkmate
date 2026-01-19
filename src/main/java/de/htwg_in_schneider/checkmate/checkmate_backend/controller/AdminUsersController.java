package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.AdminUserResponse;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUsersController {

    private final UserRepository userRepo;

    public AdminUsersController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal Jwt jwt) {

        // 1) eingeloggten User laden
        User requester = userRepo.findByOauthId(jwt.getSubject()).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        }

        // 2) Admin check
        if (requester.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin only"));
        }

        // 3) Alle User → DTO
        List<AdminUserResponse> result = userRepo.findAll().stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getOauthId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}