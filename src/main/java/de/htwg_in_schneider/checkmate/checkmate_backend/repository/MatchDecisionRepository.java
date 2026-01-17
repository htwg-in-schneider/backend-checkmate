package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph; // WICHTIG
import org.springframework.data.jpa.repository.JpaRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.MatchDecision;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;

public interface MatchDecisionRepository extends JpaRepository<MatchDecision, Long> {
    
    Optional<MatchDecision> findByFromUser_IdAndToUser_Id(Long fromId, Long toId);

    boolean existsByFromUser_IdAndToUser_IdAndDecision(Long fromId, Long toId, MatchDecision.Decision decision);

    // ✅ Ergänze den EntityGraph, damit der toUser direkt mitgeladen wird
    @EntityGraph(attributePaths = {"toUser"})
    List<MatchDecision> findByFromUser_IdAndDecision(Long fromId, MatchDecision.Decision decision);
}