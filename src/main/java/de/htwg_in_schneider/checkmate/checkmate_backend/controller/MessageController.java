package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.DirectMessage;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Message;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.DirectMessageRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.MessageRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/my/messages")
public class MessageController {

    private final MessageRepository messageRepo;
    private final TutorRepository tutorRepo;
    private final UserRepository userRepo;
    private final DirectMessageRepository directRepo;

    public MessageController(MessageRepository messageRepo,
                             TutorRepository tutorRepo,
                             UserRepository userRepo,
                             DirectMessageRepository directRepo) {
        this.messageRepo = messageRepo;
        this.tutorRepo = tutorRepo;
        this.userRepo = userRepo;
        this.directRepo = directRepo;
    }

    // ============
    // THREADS LIST (Tutor + User)
    // GET /api/my/messages/threads
    // ============
    @GetMapping("/threads")
    public ResponseEntity<?> myThreads(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        String me = jwt.getSubject();

        // ---------- Tutor Threads ----------
        List<Message> tutorAll = messageRepo.findByStudentOauthIdOrderByCreatedAtDesc(me);

        Map<Long, Message> latestByTutor = new LinkedHashMap<>();
        for (Message m : tutorAll) latestByTutor.putIfAbsent(m.getTutorId(), m);

        List<Long> tutorIds = new ArrayList<>(latestByTutor.keySet());
        Map<Long, String> tutorNames = tutorRepo.findAllById(tutorIds).stream()
                .collect(Collectors.toMap(Tutor::getId, Tutor::getName));

        List<Map<String, Object>> tutorThreads = latestByTutor.entrySet().stream().map(e -> {
            Long tutorId = e.getKey();
            Message last = e.getValue();
            Map<String, Object> dto = new HashMap<>();
            dto.put("type", "TUTOR");
            dto.put("tutorId", tutorId);
            dto.put("title", tutorNames.getOrDefault(tutorId, "Tutor #" + tutorId));
            dto.put("lastText", last.getText());
            dto.put("updatedAt", last.getCreatedAt());
            return dto;
        }).toList();

        // ---------- User Threads (Direct) ----------
        List<DirectMessage> directAll =
                directRepo.findBySenderOauthIdOrReceiverOauthIdOrderByCreatedAtDesc(me, me);

        Map<String, DirectMessage> latestByOther = new LinkedHashMap<>();
        for (DirectMessage dm : directAll) {
            String other = me.equals(dm.getSenderOauthId()) ? dm.getReceiverOauthId() : dm.getSenderOauthId();
            latestByOther.putIfAbsent(other, dm);
        }

        List<String> otherIds = new ArrayList<>(latestByOther.keySet());
        Map<String, String> otherNames = userRepo.findByOauthIdIn(otherIds).stream()
                .collect(Collectors.toMap(
                        User::getOauthId,
                        u -> (u.getName() == null || u.getName().isBlank()) ? "Match" : u.getName()
                ));

        List<Map<String, Object>> userThreads = latestByOther.entrySet().stream().map(e -> {
            String otherId = e.getKey();
            DirectMessage last = e.getValue();
            Map<String, Object> dto = new HashMap<>();
            dto.put("type", "USER");
            dto.put("userOauthId", otherId);
            dto.put("title", otherNames.getOrDefault(otherId, "Match"));
            dto.put("lastText", last.getText());
            dto.put("updatedAt", last.getCreatedAt());
            return dto;
        }).toList();

        // ---------- Combine + sort desc by updatedAt ----------
        List<Map<String, Object>> combined = new ArrayList<>();
        combined.addAll(tutorThreads);
        combined.addAll(userThreads);

        combined.sort((a, b) -> {
            Object da = a.get("updatedAt");
            Object db = b.get("updatedAt");

            if (da instanceof LocalDateTime la && db instanceof LocalDateTime lb) {
                return lb.compareTo(la);
            }
            return 0;
        });

        return ResponseEntity.ok(combined);
    }

    // ============
    // DIRECT CONVERSATION (User <-> User)
    // GET /api/my/messages/users/{otherOauthId}
    // ============
    @GetMapping("/users/{otherOauthId}")
    public ResponseEntity<?> myUserConversation(@AuthenticationPrincipal Jwt jwt,
                                                @PathVariable String otherOauthId) {
        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        String me = jwt.getSubject();
        if (otherOauthId == null || otherOauthId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "otherOauthId missing"));
        }

        return ResponseEntity.ok(directRepo.findConversation(me, otherOauthId));
    }

    // ============
    // TUTOR CONVERSATION
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
    // SEND BODY
    // ============
    public static class SendBody {
        public String text;
    }

    // ============
    // SEND TO TUTOR
    // POST /api/my/messages/tutors/{tutorId}
    // ============
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

        User student = userRepo.findByOauthId(studentSub).orElse(null);
        String senderName = (student != null && student.getName() != null && !student.getName().isBlank())
                ? student.getName()
                : "Student";

        Message m = new Message();
        m.setTutorId(tutorId);
        m.setStudentOauthId(studentSub);
        m.setSender(Message.Sender.STUDENT);
        m.setSenderName(senderName);
        m.setText(text.trim());

        return ResponseEntity.ok(messageRepo.save(m));
    }

    // ============
    // SEND TO USER (Match)
    // POST /api/my/messages/users/{otherOauthId}
    // ============
    @PostMapping("/users/{otherOauthId}")
    public ResponseEntity<?> sendToUser(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable String otherOauthId,
                                        @RequestBody SendBody body) {
        if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        String me = jwt.getSubject();
        String text = body == null ? null : body.text;

        if (otherOauthId == null || otherOauthId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "otherOauthId missing"));
        }
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "text must not be empty"));
        }
        if (text.length() > 2000) {
            return ResponseEntity.badRequest().body(Map.of("message", "text too long (max 2000)"));
        }

        User sender = userRepo.findByOauthId(me).orElse(null);
        String senderName = (sender != null && sender.getName() != null && !sender.getName().isBlank())
                ? sender.getName()
                : "Student";

        DirectMessage m = new DirectMessage();
        m.setSenderOauthId(me);
        m.setReceiverOauthId(otherOauthId);
        m.setSenderName(senderName);
        m.setText(text.trim());

        return ResponseEntity.ok(directRepo.save(m));
    }
}