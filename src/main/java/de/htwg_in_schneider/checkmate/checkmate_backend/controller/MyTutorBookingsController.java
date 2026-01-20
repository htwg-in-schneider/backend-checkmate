package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Message;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.MessageRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/my")
public class MyTutorBookingsController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final TutorRepository tutorRepo;
    private final MessageRepository messageRepo;

    public MyTutorBookingsController(
            BookingRepository bookingRepo,
            UserRepository userRepo,
            TutorRepository tutorRepo,
            MessageRepository messageRepo
    ) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.tutorRepo = tutorRepo;
        this.messageRepo = messageRepo;
    }

    // Tutor sieht seine gebuchten Slots
    @GetMapping("/tutor-bookings")
    public ResponseEntity<?> myTutorBookings(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing JWT"));
        }

        String sub = jwt.getSubject();

        User u = userRepo.findByOauthId(sub).orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found in DB"));
        }

        if (u.getRole() != Role.TUTOR && u.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Only TUTOR or ADMIN can view tutor bookings"));
        }

        Tutor t = tutorRepo.findByOauthId(sub).orElse(null);
        if (t == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Booking> bookings = bookingRepo.findByTutorIdOrderByStartAtDesc(t.getId());
        return ResponseEntity.ok(bookings);
    }
    @DeleteMapping("/tutor-bookings/{id}")
    @Transactional
    public ResponseEntity<?> cancelAsTutor(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Missing JWT"));
    
        String sub = jwt.getSubject();
        User u = userRepo.findByOauthId(sub).orElse(null);
        if (u == null) return ResponseEntity.status(401).body(Map.of("message", "User not found"));
    
        boolean isAdmin = u.getRole() == Role.ADMIN;
        if (!isAdmin && u.getRole() != Role.TUTOR) {
            return ResponseEntity.status(403).body(Map.of("message", "Only TUTOR or ADMIN can cancel tutor bookings"));
        }
    
        // ✅ Booking VOR dem Löschen laden (sonst fehlen Daten für Message)
        Booking b = bookingRepo.findById(id).orElse(null);
        if (b == null) return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));
    
        String tutorName = (u.getName() != null && !u.getName().isBlank()) ? u.getName() : "Tutor";
    
        int deleted;
        if (isAdmin) {
            bookingRepo.deleteById(id);
            deleted = 1;
        } else {
            Tutor t = tutorRepo.findByOauthId(sub).orElse(null);
            if (t == null) return ResponseEntity.status(403).body(Map.of("message", "Tutor profile not found"));
    
            tutorName = (t.getName() != null && !t.getName().isBlank()) ? t.getName() : tutorName;
    
            // Tutor darf nur eigene Buchungen stornieren
            if (!java.util.Objects.equals(b.getTutorId(), t.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Not your booking"));
            }
    
            deleted = bookingRepo.deleteByTutor(id, t.getId());
        }
    
        if (deleted == 0) {
            return ResponseEntity.status(404).body(Map.of("message", "Booking not found (or not yours)"));
        }
    
        // ✅ Message erzeugen (Tutor -> Student)
        Message m = new Message();
        m.setTutorId(b.getTutorId());
        m.setStudentOauthId(b.getStudentOauthId());
        m.setSender(Message.Sender.TUTOR);
        m.setSenderName(tutorName);
    
        String text = "Stornierung: Ich muss die Stunde am "
                + b.getStartAt() + " (" + b.getDurationMinutes() + " Min) leider absagen.";
        m.setText(text);
    
        messageRepo.save(m);
    
        // ✅ studentOauthId zurückgeben, damit Frontend direkt Chat öffnen kann
        return ResponseEntity.ok(Map.of(
                "message", "Booking cancelled",
                "studentOauthId", b.getStudentOauthId()
        ));
    }
    
}