package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Transaction;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TransactionRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionsController {

    private final TransactionRepository txRepo;
    private final UserRepository userRepo;

    public AdminTransactionsController(TransactionRepository txRepo, UserRepository userRepo) {
        this.txRepo = txRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<?> all(@AuthenticationPrincipal Jwt jwt) {

        User u = userRepo.findByOauthId(jwt.getSubject()).orElse(null);
        if (u == null) return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        if (u.getRole() != Role.ADMIN) return ResponseEntity.status(403).body(Map.of("message", "Admin only"));

        List<Transaction> all = txRepo.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(all);
    }
}