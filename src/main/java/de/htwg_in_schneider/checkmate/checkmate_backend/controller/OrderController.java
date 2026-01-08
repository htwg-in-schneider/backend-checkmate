package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody Object payload) {
        System.out.println("ORDER: " + payload);
        return ResponseEntity.ok().build();
    }
}