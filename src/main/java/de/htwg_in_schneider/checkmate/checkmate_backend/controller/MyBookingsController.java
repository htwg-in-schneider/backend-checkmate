package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/my/bookings")
public class MyBookingsController {

    private final BookingRepository bookingRepo;

    public MyBookingsController(BookingRepository bookingRepo) {
        this.bookingRepo = bookingRepo;
    }

    @GetMapping
    public ResponseEntity<?> myBookings(@AuthenticationPrincipal Jwt jwt) {

        // Sicherheit: JWT muss vorhanden sein
        if (jwt == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Not authenticated"));
        }

        String sub = jwt.getSubject();

        List<Booking> bookings =
                bookingRepo.findByStudentOauthIdOrderByStartAtDesc(sub);

        return ResponseEntity.ok(bookings);
    }
}