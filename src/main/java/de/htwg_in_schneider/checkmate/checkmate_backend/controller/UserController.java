package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // GET /api/users/me
    @GetMapping("/me")
    public User getMe(@AuthenticationPrincipal Jwt jwt) {
        return userRepo.findByOauthId(jwt.getSubject())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));
    }

    // PATCH /api/users/me -> Erlaubt Teil-Updates von Name/E-Mail
    @PutMapping("/me")
    public User updateMe(@AuthenticationPrincipal Jwt jwt, @RequestBody User incoming) {
        User existing = userRepo.findByOauthId(jwt.getSubject())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        // Nur Name und Email erlauben, KEINE Rollen oder IDs!
        if (incoming.getName() != null) existing.setName(incoming.getName());
        if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());

        return userRepo.save(existing);
    }
}