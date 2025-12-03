package de.htwg_in_schneider.checkmate.checkmate_backend.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    List<Tutor> findByNameContainingIgnoreCase(String name);
    List<Tutor> findByCategory(Category category);
    List<Tutor> findByNameContainingIgnoreCaseAndCategory(String name, Category category);
 }

