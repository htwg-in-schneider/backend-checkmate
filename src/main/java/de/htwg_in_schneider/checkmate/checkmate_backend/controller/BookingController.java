package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.BookingRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;

    public BookingController(BookingRepository bookingRepo, UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        // 0) JWT muss vorhanden sein (ansonsten Security/Config Problem)
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing JWT (not authenticated)"));
        }

        // 1) eingeloggter User
        String sub = jwt.getSubject();
        User u = userRepo.findByOauthId(sub).orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found in DB"));
        }

        if (u.getRole() != Role.STUDENT && u.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Only STUDENT or ADMIN can book"));
        }

        // 2) Basic Validation (falls DTO nicht alles validiert)
        if (req.getStartAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "startAt is required"));
        }
        if (req.getDurationMinutes() == null || req.getDurationMinutes() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "durationMinutes must be > 0"));
        }
        if (req.getTutorId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "tutorId is required"));
        }

        // 3) Overlap prüfen
        LocalDateTime start = req.getStartAt();
        LocalDateTime end = start.plusMinutes(req.getDurationMinutes());

        // grobes Fenster: start-240min bis end (damit wir Kandidaten holen können)
        LocalDateTime windowStart = start.minusMinutes(240);

        List<Booking> candidates =
                bookingRepo.findByTutorIdAndStartAtLessThanAndStartAtGreaterThanEqual(
                        req.getTutorId(),
                        end,
                        windowStart
                );

        boolean overlaps = candidates.stream().anyMatch(b -> {
            LocalDateTime bStart = b.getStartAt();
            LocalDateTime bEnd = bStart.plusMinutes(b.getDurationMinutes());
            return start.isBefore(bEnd) && bStart.isBefore(end);
        });

        if (overlaps) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Time slot already booked",
                    "tutorId", req.getTutorId(),
                    "startAt", req.getStartAt().toString(),
                    "durationMinutes", req.getDurationMinutes()
            ));
        }

        // 4) speichern
        Booking booking = new Booking();
        booking.setTutorId(req.getTutorId());
        booking.setTutorName(req.getTutorName());

        booking.setStudentOauthId(u.getOauthId());
        booking.setStudentName(u.getName());

        booking.setStartAt(req.getStartAt());
        booking.setDurationMinutes(req.getDurationMinutes());
        booking.setPrice(req.getPrice());
        booking.setNote(req.getNote());

        Booking saved = bookingRepo.save(booking);
        return ResponseEntity.ok(saved);
    }
}