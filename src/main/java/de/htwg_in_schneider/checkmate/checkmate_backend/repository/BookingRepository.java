package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByOrderByStartAtDesc();

    // meine Buchungen
    List<Booking> findByStudentOauthIdOrderByStartAtDesc(String studentOauthId);

    // admin: alle Buchungen
    List<Booking> findAllByOrderByCreatedAtDesc();

    // overlap-check (wie in deinem BookingController)
    List<Booking> findByTutorIdAndStartAtLessThanAndStartAtGreaterThanEqual(
        Long tutorId,
        LocalDateTime windowEnd,
        LocalDateTime windowStart
    );

    // availability
    List<Booking> findByTutorIdAndStartAtBetween(Long tutorId, LocalDateTime from, LocalDateTime to);
}