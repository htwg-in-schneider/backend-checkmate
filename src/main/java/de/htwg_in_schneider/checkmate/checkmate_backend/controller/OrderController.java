package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.OrderRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final BookingRepository bookingRepo;

    public OrderController(BookingRepository bookingRepo) {
        this.bookingRepo = bookingRepo;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest req) {

        System.out.println("ORDER REQUEST:");
        System.out.println("tutorId=" + req.getTutorId());
        System.out.println("tutorName=" + req.getTutorName());
        System.out.println("startAt=" + req.getStartAt());
        System.out.println("durationMinutes=" + req.getDurationMinutes());
        System.out.println("price=" + req.getPrice());
        System.out.println("note=" + req.getNote());

        LocalDateTime start = LocalDateTime.parse(req.getStartAt());

        // ❌ Slot schon belegt?
        if (bookingRepo.existsByTutorIdAndStartAt(req.getTutorId(), start)) {
            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "error", "SLOT_ALREADY_BOOKED",
                            "message", "Dieser Termin ist leider schon vergeben."
                    ));
        }

        // ✅ Buchung speichern
        Booking b = new Booking();
        b.setTutorId(req.getTutorId());
        b.setStartAt(start);
        b.setDurationMinutes(req.getDurationMinutes());
        b.setNote(req.getNote());

        bookingRepo.save(b);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Booking confirmed",
                "tutorId", req.getTutorId(),
                "startAt", req.getStartAt()
        ));
    }
}