package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Student;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.StudentRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") // Hier ergänzen
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository repo;
    

    public StudentController(StudentRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Student> getAll(@AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
        // Wenn eingeloggt: Zeige alle außer mir selbst
          String myOauthId = jwt.getSubject();
          return repo.findAllByUser_OauthIdNot(myOauthId);
    }
    // Falls nicht eingeloggt (public): Zeige alle
    return repo.findAll();
}
    // ✅ Public (wenn gewollt)
    //@GetMapping
    //public List<Student> getAll() {
    //    return repo.findAll();
    //}

    // ✅ Public (wenn gewollt)
    @GetMapping("/{id}")
    public Student getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    // Private: eigenes Profil (über Auth0 sub)
   @GetMapping("/me")
public ResponseEntity<Student> getMe(@AuthenticationPrincipal Jwt jwt) {
    String oauthId = jwt.getSubject();
    
    return repo.findByUser_OauthId(oauthId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                // Optional: Erstelle hier direkt einen leeren Studenten-Eintrag in der DB,
                // falls das Frontend sofort Daten zum Bearbeiten braucht.
                return ResponseEntity.notFound().build(); 
            });
}
    //  Private: eigenes Profil updaten (nur Student-Felder)
    @PutMapping("/me")
    public Student updateMe(@AuthenticationPrincipal Jwt jwt, @RequestBody Student incoming) {
        String oauthId = jwt.getSubject();
        Student existing = repo.findByUser_OauthId(oauthId).orElseThrow();

        // nur Profilfelder updaten
        existing.setAboutMe(incoming.getAboutMe());
        existing.setFieldOfStudy(incoming.getFieldOfStudy());
        existing.setSubjects(incoming.getSubjects()); 
        existing.setSemester(incoming.getSemester());
        existing.setUniversity(incoming.getUniversity());
        existing.setImageUrl(incoming.getImageUrl());

        // NICHT existing.setUser(...)
        return repo.save(existing);
    }

    

    // ❌ POST bewusst entfernt (Student hängt an User, das ist sonst unsauber)
}
