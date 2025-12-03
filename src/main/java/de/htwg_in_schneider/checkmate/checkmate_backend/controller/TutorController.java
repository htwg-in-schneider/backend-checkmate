package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import java.util.List;

@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/tutors")
public class TutorController {
private static final Logger LOG = LoggerFactory.getLogger(TutorController.class);
    @Autowired
    private TutorRepository tutorRepository;

    @GetMapping
    public List<Tutor> getTutors(@RequestParam(required = false) String name, 
        @RequestParam(required = false) Category category) {
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
        tutor.setId(null);
        LOG.warn("Attempted to create a tutor with an existing ID. ID has been set to null to create a new tutor.");
    }
    Tutor newTutor = tutorRepository.save(tutor);
    LOG.info("Created new tutor with id " + newTutor.getId());
    return newTutor;
}

@PutMapping("/{id}")
public ResponseEntity<Tutor> updateTutor(@PathVariable Long id, @RequestBody Tutor tutorDetails) {
    Optional<Tutor> opt = tutorRepository.findById(id);
    if (!opt.isPresent()) {
        return ResponseEntity.notFound().build();
    }

    Tutor tutor = opt.get();
    tutor.setName(tutorDetails.getName());
    tutor.setSubject(tutorDetails.getSubject());
    tutor.setSemester(tutorDetails.getSemester());
    tutor.setImage(tutorDetails.getImage());

    Tutor updatedTutor = tutorRepository.save(tutor);
    LOG.info("Updated tutor with id " + updatedTutor.getId());
    return ResponseEntity.ok(updatedTutor);
}

@DeleteMapping("/{id}")
public ResponseEntity<Object> deleteTutor(@PathVariable Long id) {
    Optional<Tutor> opt = tutorRepository.findById(id);
    if (!opt.isPresent()) {
        return ResponseEntity.notFound().build();
    }

    tutorRepository.delete(opt.get());
    LOG.info("Deleted tutor with id " + id);
    return ResponseEntity.noContent().build();
}

@GetMapping("/{id}")
public ResponseEntity<Tutor> getTutorById(@PathVariable Long id) {
    Optional<Tutor> opt = tutorRepository.findById(id);
    if (opt.isPresent()) {
        return ResponseEntity.ok(opt.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}
}


   