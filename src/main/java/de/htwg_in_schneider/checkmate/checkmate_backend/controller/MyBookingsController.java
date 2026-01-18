package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.MessageRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Message;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/my/bookings")
public class MyBookingsController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    public MyBookingsController(BookingRepository bookingRepo, UserRepository userRepo, MessageRepository messageRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    // ✅ DELETE /api/my/bookings/{id}
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        User student = userRepo.findByOauthId(jwt.getSubject()).orElse(null);
        if (student == null) return ResponseEntity.status(401).body(Map.of("message", "User not found"));

        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));

        if (!student.getOauthId().equals(b.getStudentOauthId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Not your booking"));
        }

        int deleted = bookingRepo.deleteOwned(id, student.getOauthId());
        if (deleted == 0) return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));

        Message m = new Message();
        m.setTutorId(b.getTutorId());
        m.setStudentOauthId(student.getOauthId());
        m.setSender(Message.Sender.STUDENT);
        m.setSenderName(student.getName());

        String text = "Stornierung: Ich muss die Stunde am "
                + b.getStartAt() + " (" + b.getDurationMinutes() + " Min) leider absagen.";
        m.setText(text);

        messageRepo.save(m);

        return ResponseEntity.ok(Map.of("message", "Booking cancelled"));
    }

    // ✅ GET /api/my/bookings
    @GetMapping
    public ResponseEntity<?> myBookings(@AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        String sub = jwt.getSubject();
        List<Booking> bookings = bookingRepo.findByStudentOauthIdOrderByStartAtDesc(sub);

        return ResponseEntity.ok(bookings);
    }
}