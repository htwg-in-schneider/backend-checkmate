package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.AvailabilityRule;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.AvailabilityRuleRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    private static final Logger LOG = LoggerFactory.getLogger(TutorController.class);

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private AvailabilityRuleRepository availabilityRuleRepository;

    @GetMapping
    public List<Tutor> getTutors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Category category
    ) {
        if (name != null && category != null) {
            return tutorRepository.findByNameContainingIgnoreCaseAndCategory(name, category);
        } else if (name != null) {
            return tutorRepository.findByNameContainingIgnoreCase(name);
        } else if (category != null) {
            return tutorRepository.findByCategory(category);
        } else {
            return tutorRepository.findAll();
        }
    }

    @PostMapping
    public Tutor createTutor(@RequestBody Tutor tutor) {
        if (tutor.getId() != null) {
            LOG.warn("Attempted to create a tutor with an existing ID. Setting ID to null.");
            tutor.setId(null);
        }

        if (tutor.getSubject() != null) {
            tutor.setCategory(mapSubjectToCategory(tutor.getSubject()));
        }

        Tutor newTutor = tutorRepository.save(tutor);
        LOG.info("Created new tutor with id {}", newTutor.getId());
        return newTutor;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tutor> updateTutor(@PathVariable Long id, @RequestBody Tutor tutorDetails) {
        Optional<Tutor> opt = tutorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tutor tutor = opt.get();
        tutor.setName(tutorDetails.getName());
        tutor.setSubject(tutorDetails.getSubject());
        tutor.setSemester(tutorDetails.getSemester());
        tutor.setImage(tutorDetails.getImage());

        if (tutorDetails.getSubject() != null) {
            tutor.setCategory(mapSubjectToCategory(tutorDetails.getSubject()));
        }

        Tutor updatedTutor = tutorRepository.save(tutor);
        LOG.info("Updated tutor with id {}", updatedTutor.getId());

        return ResponseEntity.ok(updatedTutor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTutor(@PathVariable Long id) {
        Optional<Tutor> opt = tutorRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        tutorRepository.delete(opt.get());
        LOG.info("Deleted tutor with id {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutor> getTutorById(@PathVariable Long id) {
        return tutorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ WICHTIG: umbenannt, damit KEIN Konflikt mit AvailabilityController entsteht
    @GetMapping("/{tutorId}/raw-available-times")
    public List<String> getRawAvailableTimes(
            @PathVariable Long tutorId,
            @RequestParam String date,
            @RequestParam(defaultValue = "60") int durationMinutes
    ) {
        LocalDate d = LocalDate.parse(date);
        DayOfWeek dow = d.getDayOfWeek();

        List<AvailabilityRule> rules =
                availabilityRuleRepository.findByTutorIdAndDayOfWeek(tutorId, dow);

        List<String> times = new ArrayList<>();

        for (AvailabilityRule r : rules) {
            LocalTime t = r.getStartTime();

            while (!t.plusMinutes(durationMinutes).isAfter(r.getEndTime())) {
                times.add(String.format("%02d:%02d", t.getHour(), t.getMinute()));
                t = t.plusMinutes(30);
            }
        }

        return times.stream().distinct().sorted().toList();
    }

    private Category mapSubjectToCategory(String subject) {
        if (subject == null) return null;

        switch (subject.trim().toLowerCase()) {
            case "mathe 1":
                return Category.MATHE1;
            case "mathe 2":
                return Category.MATHE2;
            case "bwl 1":
                return Category.BWL1;
            case "bwl 2":
                return Category.BWL2;
            case "programmieren":
                return Category.PROGRAMMIEREN;
            case "englisch":
                return Category.ENGLISCH;
            default:
                return null;
        }
    }
}