package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Message;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.MessageRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageRepository messageRepo;
    private final TutorRepository tutorRepo;

    public MessageController(MessageRepository messageRepo, TutorRepository tutorRepo) {
        this.messageRepo = messageRepo;
        this.tutorRepo = tutorRepo;
    }

    // ============
    // THREADS LIST
    // GET /api/my/messages/threads
    // ============
    @GetMapping("/my/messages/threads")
    public List<Map<String, Object>> myThreads(@AuthenticationPrincipal Jwt jwt) {
        String studentSub = jwt.getSubject();

        List<Message> all = messageRepo.findByStudentOauthIdOrderByCreatedAtDesc(studentSub);

        // latest per tutor
        Map<Long, Message> latestByTutor = new LinkedHashMap<>();
        for (Message m : all) latestByTutor.putIfAbsent(m.getTutorId(), m);

        List<Long> tutorIds = new ArrayList<>(latestByTutor.keySet());
        Map<Long, String> tutorNames = tutorRepo.findAllById(tutorIds).stream()
                .collect(Collectors.toMap(Tutor::getId, Tutor::getName));

        return latestByTutor.entrySet().stream().map(e -> {
            Long tutorId = e.getKey();
            Message last = e.getValue();
            Map<String, Object> dto = new HashMap<>();
            dto.put("tutorId", tutorId);
            dto.put("tutorName", tutorNames.getOrDefault(tutorId, "Tutor #" + tutorId));
            dto.put("lastText", last.getText());
            dto.put("lastAt", last.getCreatedAt());
            return dto;
        }).toList();
    }

    // ============
    // CONVERSATION
    // GET /api/my/messages/tutors/{tutorId}
    // ============
    @GetMapping("/my/messages/tutors/{tutorId}")
    public List<Message> myConversation(@AuthenticationPrincipal Jwt jwt, @PathVariable Long tutorId) {
        String studentSub = jwt.getSubject();
        return messageRepo.findByStudentOauthIdAndTutorIdOrderByCreatedAtAsc(studentSub, tutorId);
    }

    // ============
    // SEND MESSAGE
    // POST /api/my/messages/tutors/{tutorId}
    // body: { "text": "..." }
    // ============
    public static class SendBody { public String text; }

    @PostMapping("/my/messages/tutors/{tutorId}")
    public ResponseEntity<?> send(@AuthenticationPrincipal Jwt jwt, @PathVariable Long tutorId, @RequestBody SendBody body) {
        String studentSub = jwt.getSubject();

        String text = body == null ? null : body.text;
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("text must not be empty");
        }
        if (text.length() > 2000) {
            return ResponseEntity.badRequest().body("text too long (max 2000)");
        }
        if (!tutorRepo.existsById(tutorId)) {
            return ResponseEntity.status(404).body("Tutor not found");
        }

        Message m = new Message();
        m.setTutorId(tutorId);
        m.setStudentOauthId(studentSub);
        m.setSender(Message.Sender.STUDENT);
        m.setText(text.trim());
        m.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(messageRepo.save(m));
    }
}