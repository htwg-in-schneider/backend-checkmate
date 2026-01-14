package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByStudentOauthIdAndTutorIdOrderByCreatedAtAsc(String studentOauthId, Long tutorId);

    List<Message> findByStudentOauthIdOrderByCreatedAtDesc(String studentOauthId);
}