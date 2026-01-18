package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // Gespräch zwischen A und B (in beide Richtungen)
    @Query("""
      select m from DirectMessage m
      where (m.senderOauthId = :a and m.receiverOauthId = :b)
         or (m.senderOauthId = :b and m.receiverOauthId = :a)
      order by m.createdAt asc
    """)
    List<DirectMessage> findConversation(@Param("a") String a, @Param("b") String b);

    // Alle Messages, wo User beteiligt ist (für Threads)
    List<DirectMessage> findBySenderOauthIdOrReceiverOauthIdOrderByCreatedAtDesc(String sender, String receiver);
}