package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Student;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.StudentRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Hier ergänzen
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository repo;
    private UserRepository userRepo;

    public StudentController(StudentRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }
    @PostMapping
    @Transactional // ✅ Wichtig für die Speicherung in zwei Tabellen
    public Student createStudent(@RequestBody Map<String, Object> payload) {
        
        // 1. User-Objekt erstellen (Identität)
        User newUser = new User();
        newUser.setName((String) payload.get("name"));
        newUser.setEmail((String) payload.get("email"));
        newUser.setRole(Role.STUDENT);
        
        // ✅ Essentiell: Da oauthId in der DB nicht null sein darf
        newUser.setOauthId("manual|" + System.currentTimeMillis()); 
        
        User savedUser = userRepo.save(newUser); // ✅ Jetzt ist userRepo nicht mehr null

        // 2. Student-Objekt erstellen (Profil)
        Student student = new Student();
        student.setUser(savedUser); // ✅ Verknüpfung über @MapsId
        student.setAboutMe((String) payload.get("aboutMe"));
        student.setFieldOfStudy((String) payload.get("fieldOfStudy"));
        student.setUniversity((String) payload.get("university"));
        student.setImageUrl((String) payload.get("imageUrl"));

        // ✅ Sicherer Umgang mit Semester-Zahl (verhindert Cast-Exception)
        if (payload.get("semester") != null) {
            student.setSemester(Integer.parseInt(payload.get("semester").toString()));
        }

        if (payload.containsKey("subjects") && payload.get("subjects") instanceof List) {
            student.setSubjects((List<String>) payload.get("subjects"));
        }

        return repo.save(student);
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
public ResponseEntity<Student> updateMe(@AuthenticationPrincipal Jwt jwt, @RequestBody Student incoming) {
    if (jwt == null) return ResponseEntity.status(401).build();

    String oauthId = jwt.getSubject();
    Student existing = repo.findByUser_OauthId(oauthId).orElseThrow();

    existing.setAboutMe(incoming.getAboutMe());
    existing.setFieldOfStudy(incoming.getFieldOfStudy());
    existing.setSubjects(incoming.getSubjects());
    existing.setSemester(incoming.getSemester());
    existing.setUniversity(incoming.getUniversity());
    existing.setImageUrl(incoming.getImageUrl());

    return ResponseEntity.ok(repo.save(existing));
}
}
