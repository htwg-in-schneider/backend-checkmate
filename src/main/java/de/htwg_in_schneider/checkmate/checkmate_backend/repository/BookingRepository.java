package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
// BookingRepository.java
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @Modifying
    @Query("delete from Booking b where b.id = :id and b.studentOauthId = :studentOauthId")
    int deleteOwned(@Param("id") Long id, @Param("studentOauthId") String studentOauthId);
  

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