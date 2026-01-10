package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload) {
        System.out.println("BOOKING: " + payload);
        return ResponseEntity.ok(payload); // fürs Debuggen
    }
}