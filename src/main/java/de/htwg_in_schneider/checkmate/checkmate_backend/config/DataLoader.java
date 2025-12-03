package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Review;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.ReviewRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadData(TutorRepository tutorRepository,
                                      ReviewRepository reviewRepository) {
        return args -> {
            // Wenn schon Tutor:innen da sind → nichts tun
            if (tutorRepository.count() > 0) {
                LOG.info("Database already contains tutors. Skipping initial data load.");
                return;
            }

            LOG.info("Database empty. Loading initial tutor data…");

            // ---------- Tutor:innen anlegen (OHNE IDs setzen!) ----------
            Tutor lisa = new Tutor();
            lisa.setName("Lisa Weber");
            lisa.setSubject("Mathe I");
            lisa.setSemester(5);
            lisa.setImage("https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=500&q=60");
            lisa.setCategory(Category.MATHE1);

            Tutor jonas = new Tutor();
            jonas.setName("Jonas Keller");
            jonas.setSubject("Programmierung mit Java");
            jonas.setSemester(3);
            jonas.setImage("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=500&q=60");
            jonas.setCategory(Category.PROGRAMMIEREN);

            Tutor mia = new Tutor();
            mia.setName("Mia Hoffmann");
            mia.setSubject("BWL Grundlagen");
            mia.setSemester(4);
            mia.setImage("https://images.unsplash.com/photo-1544725176-7c40e5a2c9f1?auto=format&fit=crop&w=500&q=60");
            mia.setCategory(Category.BWL1);

            // In DB speichern – IDs werden von JPA vergeben
            List<Tutor> savedTutors = tutorRepository.saveAll(Arrays.asList(lisa, jonas, mia));
            LOG.info("Saved {} tutors.", savedTutors.size());

            // Zur Übersicht:
            Tutor savedLisa = savedTutors.get(0);
            Tutor savedJonas = savedTutors.get(1);
            Tutor savedMia = savedTutors.get(2);

            // ---------- Reviews anlegen ----------
            Review r1a = new Review();
            r1a.setStars(5);
            r1a.setText("Lisa erklärt Mathe super verständlich!");
            r1a.setUserName("Anna");
            r1a.setTutor(savedLisa);

            Review r1b = new Review();
            r1b.setStars(4);
            r1b.setText("Hat mir sehr bei der Klausurvorbereitung geholfen.");
            r1b.setUserName("Oli");
            r1b.setTutor(savedLisa);

            Review r2 = new Review();
            r2.setStars(5);
            r2.setText("Jonas macht Java endlich logisch 😅");
            r2.setUserName("Ben");
            r2.setTutor(savedJonas);

            Review r3 = new Review();
            r3.setStars(4);
            r3.setText("Gute BWL-Erklärungen, viel Praxisbezug.");
            r3.setUserName("Chris");
            r3.setTutor(savedMia);

            reviewRepository.saveAll(Arrays.asList(r1a, r1b, r2, r3));
            LOG.info("Initial tutor + review data loaded successfully.");
        };
    }
}