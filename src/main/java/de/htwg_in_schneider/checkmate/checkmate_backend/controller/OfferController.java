package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.OfferCreateRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.dto.OfferResponse;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Offer;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.OfferRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;

    public OfferController(OfferRepository offerRepository,
                           UserRepository userRepository,
                           TutorRepository tutorRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
    }

    @PostMapping
    public ResponseEntity<OfferResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                @RequestBody OfferCreateRequest body) {
        if (jwt == null) return ResponseEntity.status(401).build();
        String me = jwt.getSubject();
        

        // 1) User laden + Rolle prüfen
        User u = userRepository.findByOauthId(me)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (u.getRole() != Role.TUTOR && u.getRole() != Role.ADMIN) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nur Tutor:innen dürfen Angebote erstellen.");
        }

        // 2) Tutor-Profil zu Auth0-User finden oder anlegen (nur weil Rolle Tutor/Admin!)
        Tutor t = tutorRepository.findByOauthId(me).orElseGet(() -> {
            Tutor nt = new Tutor();
            nt.setOauthId(me);
            nt.setName((u.getName() != null && !u.getName().isBlank()) ? u.getName() : "Tutor");
            // optional defaults:
            // nt.setSubject("...");
            // nt.setSemester(1);
            // nt.setImage(null);
            return tutorRepository.save(nt);
        });

        // 3) Offer speichern (wichtig: ownerSub setzen!)
        Offer o = new Offer();
        o.setOwnerSub(me);
        o.setTutorId(t.getId());
        o.setSubject(body.subject);
        o.setSemester(body.semester != null ? body.semester : 1);
        o.setHourlyRate(body.hourlyRate);

        Offer saved = offerRepository.save(o);

        OfferResponse resp = toResponse(saved);

        // 201 Created + Location Header
        return ResponseEntity
                .created(URI.create("/api/offers/" + saved.getId()))
                .body(resp);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OfferResponse>> getMyOffers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).build();
        String sub = jwt.getSubject();

        List<OfferResponse> result = offerRepository.findByOwnerSubOrderByCreatedAtDesc(sub)
                .stream().map(this::toResponse).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public List<OfferResponse> getAllOffers() {
        return offerRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (jwt == null) return ResponseEntity.status(401).build();
        String sub = jwt.getSubject();

        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();

        // Admin darf alles löschen (optional, aber praktisch)
        User u = userRepository.findByOauthId(sub).orElse(null);
        boolean isAdmin = (u != null && u.getRole() == Role.ADMIN);

        if (!isAdmin && !sub.equals(offer.getOwnerSub())) {
            return ResponseEntity.status(403).build();
        }

        offerRepository.delete(offer);
        return ResponseEntity.noContent().build();
    }

    private OfferResponse toResponse(Offer o) {
        OfferResponse r = new OfferResponse();
        r.id = o.getId();
        r.tutorId = o.getTutorId();
        r.subject = o.getSubject();
        r.semester = o.getSemester();
        r.hourlyRate = o.getHourlyRate();
        r.createdAt = o.getCreatedAt();

        var userOpt = userRepository.findByOauthId(o.getOwnerSub());
        r.ownerName = userOpt.map(User::getName).orElse("Tutor");
        r.ownerEmail = userOpt.map(User::getEmail).orElse(null);

        return r;
    }
}