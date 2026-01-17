package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.OfferCreateRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.dto.OfferResponse;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Offer;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.OfferRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferRepository offerRepository;

    public OfferController(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    // ========== CREATE ==========
    // POST /api/offers
    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody OfferCreateRequest req
    ) {
        // Auth0 "sub" = eindeutige User-ID
        String ownerSub = jwt.getSubject();

        // Basic Validation
        if (req.title == null || req.title.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (req.subject == null || req.subject.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Offer offer = new Offer();
        offer.setOwnerSub(ownerSub);
        offer.setTitle(req.title.trim());
        offer.setSubject(req.subject.trim());
        offer.setDescription(req.description);
        offer.setHourlyRate(req.hourlyRate != null ? req.hourlyRate : 20);
        offer.setDurationMinutes(req.durationMinutes != null ? req.durationMinutes : 60);
        offer.setLocation(req.location != null ? req.location : "Online");

        Offer saved = offerRepository.save(offer);

        return ResponseEntity
                .created(URI.create("/api/offers/" + saved.getId()))
                .body(toResponse(saved));
    }

    // ========== READ MINE ==========
    // GET /api/offers/mine
    @GetMapping("/mine")
    public List<OfferResponse> getMyOffers(@AuthenticationPrincipal Jwt jwt) {
        String ownerSub = jwt.getSubject();
        return offerRepository.findByOwnerSubOrderByCreatedAtDesc(ownerSub)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ========== DELETE ==========
    // DELETE /api/offers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        String requesterSub = jwt.getSubject();

        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) {
            return ResponseEntity.notFound().build();
        }

        // nur Besitzer darf löschen (Admin-Erweiterung kann später rein)
        if (!offer.getOwnerSub().equals(requesterSub)) {
            return ResponseEntity.status(403).build();
        }

        offerRepository.delete(offer);
        return ResponseEntity.noContent().build();
    }

    private OfferResponse toResponse(Offer o) {
        OfferResponse r = new OfferResponse();
        r.id = o.getId();
        r.title = o.getTitle();
        r.subject = o.getSubject();
        r.description = o.getDescription();
        r.hourlyRate = o.getHourlyRate();
        r.durationMinutes = o.getDurationMinutes();
        r.location = o.getLocation();
        r.createdAt = o.getCreatedAt();
        return r;
    }
}