package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.TransactionCheckoutRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Transaction;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TransactionRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final BookingRepository bookingRepo;
    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;

    public TransactionController(
            BookingRepository bookingRepo,
            TransactionRepository transactionRepo,
            UserRepository userRepo
    ) {
        this.bookingRepo = bookingRepo;
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(
            @Valid @RequestBody TransactionCheckoutRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        // 1) eingeloggter User
        User u = userRepo.findByOauthId(jwt.getSubject()).orElse(null);
        if (u == null) return ResponseEntity.status(401).body(Map.of("message", "User not found"));
        if (u.getRole() != Role.STUDENT && u.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Only STUDENT can checkout"));
        }

        // 2) Transaction anlegen
        Transaction tx = new Transaction();
        tx.setStudentOauthId(u.getOauthId());
        tx.setStudentName(u.getName());
        tx.setBuyerEmail(req.getBuyerEmail());
        tx.setNote(req.getNote());

        tx = transactionRepo.save(tx);

        double sum = 0.0;

        // 3) Alle Bookings prüfen + speichern (Rollback bei Fehler)
        for (var bReq : req.getBookings()) {
            LocalDateTime start = bReq.getStartAt();
            LocalDateTime end = start.plusMinutes(bReq.getDurationMinutes());

            // Kandidaten in einem Fenster holen
            LocalDateTime windowStart = start.minusMinutes(240);

            List<Booking> candidates = bookingRepo.findByTutorIdAndStartAtLessThanAndStartAtGreaterThanEqual(
                    bReq.getTutorId(),
                    end,
                    windowStart
            );

            boolean overlaps = candidates.stream().anyMatch(existing -> {
                LocalDateTime eStart = existing.getStartAt();
                LocalDateTime eEnd = eStart.plusMinutes(existing.getDurationMinutes());
                return start.isBefore(eEnd) && eStart.isBefore(end);
            });

            if (overlaps) {
                // 409 -> Frontend kann "Termin nicht mehr verfügbar" anzeigen
                return ResponseEntity.status(409).body(Map.of(
                        "message", "Time slot already booked",
                        "tutorId", bReq.getTutorId(),
                        "startAt", String.valueOf(bReq.getStartAt()),
                        "durationMinutes", bReq.getDurationMinutes()
                ));
            }

            Booking booking = new Booking();
            booking.setTutorId(bReq.getTutorId());
            booking.setTutorName(bReq.getTutorName());

            booking.setStudentOauthId(u.getOauthId());
            booking.setStudentName(u.getName());

            booking.setStartAt(start);
            booking.setDurationMinutes(bReq.getDurationMinutes());
            booking.setPrice(bReq.getPrice());
            booking.setNote(bReq.getNote());

            booking.setTransactionId(tx.getId()); // ✅ Verknüpfung

            bookingRepo.save(booking);

            sum += bReq.getPrice();
        }

        // 4) total speichern
        tx.setTotalPrice(sum);
        transactionRepo.save(tx);

        return ResponseEntity.ok(Map.of(
                "transactionId", tx.getId(),
                "totalPrice", tx.getTotalPrice()
        ));
    }
}