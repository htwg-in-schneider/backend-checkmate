package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTutorIdAndStartAtBetween(Long tutorId, LocalDateTime from, LocalDateTime to);
    boolean existsByTutorIdAndStartAt(Long tutorId, LocalDateTime startAt);
}