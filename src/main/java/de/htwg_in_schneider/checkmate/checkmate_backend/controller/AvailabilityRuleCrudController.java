package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.AvailabilityRuleRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.AvailabilityRule;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.AvailabilityRuleRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;


import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tutors")
public class AvailabilityRuleCrudController {

    private final AvailabilityRuleRepository ruleRepo;
    private final UserRepository userRepo;
    private final TutorRepository tutorRepo;

    public AvailabilityRuleCrudController(
            AvailabilityRuleRepository ruleRepo,
            UserRepository userRepo,
            TutorRepository tutorRepo
    ) {
        this.ruleRepo = ruleRepo;
        this.userRepo = userRepo;
        this.tutorRepo = tutorRepo;
    }

    // Optional: Tutor kann seine gespeicherten Regeln sehen
    @GetMapping("/{tutorId}/availability-rules")
    public List<AvailabilityRule> getRules(@PathVariable Long tutorId) {
        return ruleRepo.findByTutorId(tutorId);
    }

    // Tutor speichert seine Regeln (ersetzen: delete + insert)
    @PutMapping("/{tutorId}/availability-rules")
    @Transactional
    public ResponseEntity<?> setRules(
            @PathVariable Long tutorId,
            @RequestBody List<AvailabilityRuleRequest> rules,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(401).build();
        String sub = jwt.getSubject();

        User u = userRepo.findByOauthId(sub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        boolean isAdmin = u.getRole() == Role.ADMIN;

        // TutorId muss zu diesem eingeloggten Tutor gehören (außer Admin)
        if (!isAdmin) {
            Tutor t = tutorRepo.findByOauthId(sub)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tutor profile not found"));
            if (!t.getId().equals(tutorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your tutorId");
            }
            if (u.getRole() != Role.TUTOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only tutors can set availability");
            }
        }

        // Basic validation
        if (rules == null) rules = List.of();
        for (AvailabilityRuleRequest r : rules) {
            if (r.dayOfWeek == null || r.startTime == null || r.endTime == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek/startTime/endTime required");
            }
            LocalTime st = LocalTime.parse(r.startTime);
            LocalTime en = LocalTime.parse(r.endTime);
            if (!en.isAfter(st)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
            }
        }

        // Replace all rules
        ruleRepo.deleteByTutorId(tutorId);

        for (AvailabilityRuleRequest r : rules) {
            AvailabilityRule ar = new AvailabilityRule();
            ar.setTutorId(tutorId);
            ar.setDayOfWeek(DayOfWeek.valueOf(r.dayOfWeek));
            ar.setStartTime(LocalTime.parse(r.startTime));
            ar.setEndTime(LocalTime.parse(r.endTime));
            ar.setSlotMinutes(r.slotMinutes != null ? r.slotMinutes : 30);
            ruleRepo.save(ar);
        }

        return ResponseEntity.ok().build();
    }
}