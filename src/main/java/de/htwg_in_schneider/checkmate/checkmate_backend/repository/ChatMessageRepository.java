package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByTutorIdAndStudentOauthIdOrderByCreatedAtAsc(Long tutorId, String studentOauthId);

    List<ChatMessage> findByTutorIdAndStudentOauthIdAndCreatedAtAfterOrderByCreatedAtAsc(
            Long tutorId, String studentOauthId, LocalDateTime after
    );
}