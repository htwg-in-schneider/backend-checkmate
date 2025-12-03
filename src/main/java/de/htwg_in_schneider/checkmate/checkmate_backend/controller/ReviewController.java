package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Review;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.ReviewRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TutorRepository tutorRepository;

    // GET /api/review
    @GetMapping
    public List<Review> getAllReviews() {
        LOG.info("Fetching all reviews");
        List<Review> reviews = reviewRepository.findAll();
        LOG.info("Found {} reviews", reviews != null ? reviews.size() : 0);
        return reviews;
    }

    // GET /api/review/tutor/{tutorId}
    @GetMapping("/tutor/{tutorId}")
    public List<Review> getReviewsByTutor(@PathVariable Long tutorId) {
        LOG.info("Fetching reviews for tutor id {}", tutorId);
        List<Review> reviews = reviewRepository.findByTutorId(tutorId);
        LOG.info("Found {} reviews for tutor {}", reviews != null ? reviews.size() : 0, tutorId);
        return reviews;
    }

    // POST /api/review
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Long tutorId = null;
        if (review != null && review.getTutor() != null) {
            tutorId = review.getTutor().getId();
        }
        LOG.info("Attempting to create review for tutor id {}", tutorId);

        if (review == null) {
            LOG.warn("Review payload is null");
            return ResponseEntity.badRequest().build();
        }

        int stars = review.getStars();
        if (stars < 1 || stars > 5) {
            LOG.warn("Review stars out of bounds: {}", stars);
            return ResponseEntity.badRequest().build();
        }

        if (review.getTutor() == null || review.getTutor().getId() == null) {
            LOG.warn("Review tutor is null or has no id");
            return ResponseEntity.badRequest().build();
        }

        Tutor tutor = tutorRepository.findById(review.getTutor().getId()).orElse(null);
        if (tutor == null) {
            LOG.warn("Tutor not found for review: {}", review.getTutor().getId());
            return ResponseEntity.badRequest().build();
        }

        review.setTutor(tutor);
        Review saved = reviewRepository.save(review);
        LOG.info("Created review with id {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    // DELETE /api/review/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteReview(@PathVariable Long id) {
        LOG.info("Attempting to delete review with id {}", id);
        Review review = reviewRepository.findById(id).orElse(null);
        if (review != null) {
            reviewRepository.delete(review);
            LOG.info("Deleted review with id {}", id);
            return ResponseEntity.noContent().build();
        } else {
            LOG.warn("Review not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
