package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.ChatMessage;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.ChatMessageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatMessageRepository repo;

    public ChatController(ChatMessageRepository repo) {
        this.repo = repo;
    }

    // Alle Messages für tutor + eingeloggten user
    @GetMapping("/{tutorId}")
    public List<ChatMessage> getThread(@PathVariable Long tutorId, Authentication auth) {
        String studentOauthId = auth.getName(); // meist Auth0 sub
        return repo.findByTutorIdAndStudentOauthIdOrderByCreatedAtAsc(tutorId, studentOauthId);
    }

    // Optional: nur Messages nach Zeitpunkt (für Polling effizient)
    @GetMapping("/{tutorId}/since")
    public List<ChatMessage> getThreadSince(
            @PathVariable Long tutorId,
            @RequestParam String afterIso,
            Authentication auth
    ) {
        String studentOauthId = auth.getName();
        LocalDateTime after = LocalDateTime.parse(afterIso);
        return repo.findByTutorIdAndStudentOauthIdAndCreatedAtAfterOrderByCreatedAtAsc(tutorId, studentOauthId, after);
    }

    // Message senden
    @PostMapping("/{tutorId}")
    public ChatMessage send(
            @PathVariable Long tutorId,
            @RequestBody SendChatMessageRequest req,
            Authentication auth
    ) {
        String studentOauthId = auth.getName();

        ChatMessage m = new ChatMessage();
        m.setTutorId(tutorId);
        m.setStudentOauthId(studentOauthId);
        m.setSenderRole("STUDENT");
        m.setMessage(req.message());
        m.setCreatedAt(LocalDateTime.now());

        return repo.save(m);
    }

    public record SendChatMessageRequest(String message) {}
}
