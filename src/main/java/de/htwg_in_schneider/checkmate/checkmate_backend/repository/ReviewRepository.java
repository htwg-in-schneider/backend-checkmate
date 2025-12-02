package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTutorId(Long TutorId);
}