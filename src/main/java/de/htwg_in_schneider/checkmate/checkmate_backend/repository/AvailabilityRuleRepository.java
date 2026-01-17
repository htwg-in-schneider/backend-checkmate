package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.AvailabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRuleRepository
        extends JpaRepository<AvailabilityRule, Long> {

    // Für Controller:
    List<AvailabilityRule> findByTutorId(Long tutorId);

    // Für Slot-Generierung:
    List<AvailabilityRule> findByTutorIdAndDayOfWeek(Long tutorId, DayOfWeek dayOfWeek);

    // Für Regeln ersetzen:
    void deleteByTutorId(Long tutorId);
}