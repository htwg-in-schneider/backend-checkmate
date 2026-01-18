package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Message;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.MessageRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/my/messages")
public class MessageController {

    private final MessageRepository messageRepo;
    private final TutorRepository tutorRepo;
    private final UserRepository userRepo;

    public MessageController(MessageRepository messageRepo, TutorRepository tutorRepo, UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.tutorRepo = tutorRepo;
        this.userRepo = userRepo;
    }

    // ============
    // THREADS LIST
    // GET /api/my/messages/threads
    // ============
    @GetMapping("/threads")
    public ResponseEntity<?> myThreads(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        String studentSub = jwt.getSubject();
        List<Message> all = messageRepo.findByStudentOauthIdOrderByCreatedAtDesc(studentSub);

        // latest per tutor (all is already sorted desc)
        Map<Long, Message> latestByTutor = new LinkedHashMap<>();
        for (Message m : all) latestByTutor.putIfAbsent(m.getTutorId(), m);

        List<Long> tutorIds = new ArrayList<>(latestByTutor.keySet());
        Map<Long, String> tutorNames = tutorRepo.findAllById(tutorIds).stream()
                .collect(Collectors.toMap(Tutor::getId, Tutor::getName));

        List<Map<String, Object>> result = latestByTutor.entrySet().stream().map(e -> {
            Long tutorId = e.getKey();
            Message last = e.getValue();
            Map<String, Object> dto = new HashMap<>();
            dto.put("tutorId", tutorId);
            dto.put("tutorName", tutorNames.getOrDefault(tutorId, "Tutor #" + tutorId));
            dto.put("lastText", last.getText());
            dto.put("updatedAt", last.getCreatedAt()); // Frontend erwartet updatedAt
            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // ============
    // CONVERSATION
    // GET /api/my/messages/tutors/{tutorId}
    // ============
    @GetMapping("/tutors/{tutorId}")
    public ResponseEntity<?> myConversation(@AuthenticationPrincipal Jwt jwt, @PathVariable Long tutorId) {
        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        String studentSub = jwt.getSubject();
        if (!tutorRepo.existsById(tutorId)) {
            return ResponseEntity.status(404).body(Map.of("message", "Tutor not found"));
        }

        return ResponseEntity.ok(
                messageRepo.findByStudentOauthIdAndTutorIdOrderByCreatedAtAsc(studentSub, tutorId)
        );
    }

    // ============
    // SEND MESSAGE
    // POST /api/my/messages/tutors/{tutorId}
    // body: { "text": "..." }
    // ============
    public static class SendBody {
        public String text;
    }

    @PostMapping("/tutors/{tutorId}")
    public ResponseEntity<?> send(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable Long tutorId,
                                  @RequestBody SendBody body) {

        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        String studentSub = jwt.getSubject();

        String text = body == null ? null : body.text;
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "text must not be empty"));
        }
        if (text.length() > 2000) {
            return ResponseEntity.badRequest().body(Map.of("message", "text too long (max 2000)"));
        }
        if (!tutorRepo.existsById(tutorId)) {
            return ResponseEntity.status(404).body(Map.of("message", "Tutor not found"));
        }

        // ✅ Student-Name aus app_user holen (fallback, falls nicht vorhanden)
        User student = userRepo.findByOauthId(studentSub).orElse(null);
        String senderName = (student != null && student.getName() != null && !student.getName().isBlank())
                ? student.getName()
                : "Student";

        Message m = new Message();
        m.setTutorId(tutorId);
        m.setStudentOauthId(studentSub);
        m.setSender(Message.Sender.STUDENT);

        // ✅ Dafür brauchst du in Message.java:
        // @Column(nullable = false, length = 200)
        // private String senderName;
        m.setSenderName(senderName);

        m.setText(text.trim());
        // createdAt wird per @PrePersist gesetzt (du kannst es auch manuell setzen, musst aber nicht)

        return ResponseEntity.ok(messageRepo.save(m));
    }
}