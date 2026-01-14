package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingsController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;

    public AdminBookingsController(BookingRepository bookingRepo, UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing token"));
        }

        User u = userRepo.findByOauthId(jwt.getSubject()).orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        }
        if (u.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin only"));
        }

        // ✅ Sortiert nach Startzeit (neueste zuerst)
        List<Booking> all = bookingRepo.findAllByOrderByStartAtDesc();
        return ResponseEntity.ok(all);
    }
}