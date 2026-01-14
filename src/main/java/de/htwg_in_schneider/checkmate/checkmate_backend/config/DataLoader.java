package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Review;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.AvailabilityRule;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.ReviewRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.AvailabilityRuleRepository;

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

    // ✅ Auth0 "sub" IDs (Iteration 13b)
    // Ersetze diese Strings mit den echten sub-Werten aus eurer /profile Debug-Ansicht im Frontend.
    private static final String STUDENT_SUB = "auth0|695e5f38bd9509a108b5604d";
    private static final String TUTOR_SUB   = "auth0|695e66fcd58fa9152ab1d6f8";
    private static final String ADMIN_SUB   = "auth0|695fda2b6f4f6b2870b04cbd";


    @Bean
    public CommandLineRunner loadData(TutorRepository tutorRepository,
                                     AvailabilityRuleRepository availabilityRuleRepository,
                                      ReviewRepository reviewRepository,
                                      UserRepository userRepository) {
        return args -> {

         
            seedUsers(userRepository);

            if (tutorRepository.count() > 0) {
                LOG.info("Database already contains tutors. Skipping initial tutor/review data load.");
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
            lisa.setHourlyRate(25.0);
            lisa.setEmail("j.dauth@outlook.com");

            Tutor jonas = new Tutor();
            jonas.setName("Jonas Keller");
            jonas.setSubject("Programmierung mit Java");
            jonas.setSemester(3);
            jonas.setImage("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=500&q=60");
            jonas.setCategory(Category.PROGRAMMIEREN);
            jonas.setHourlyRate(20.0);
            jonas.setEmail("j.dauth@outlook.com");


            Tutor mia = new Tutor();
            mia.setName("Mia Hoffmann");
            mia.setSubject("BWL Grundlagen");
            mia.setSemester(4);
            mia.setImage("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80");
            mia.setCategory(Category.BWL1);
            mia.setHourlyRate(22.0);
            mia.setEmail("j.dauth@outlook.com");


            // In DB speichern – IDs werden von JPA vergeben
            List<Tutor> savedTutors = tutorRepository.saveAll(Arrays.asList(lisa, jonas, mia));
            LOG.info("Saved {} tutors.", savedTutors.size());

            // Zur Übersicht:
            Tutor savedLisa = savedTutors.get(0);
            Tutor savedJonas = savedTutors.get(1);
            Tutor savedMia = savedTutors.get(2);

            AvailabilityRule ar1 = new AvailabilityRule();
            ar1.setTutorId(savedJonas.getId());
            ar1.setDayOfWeek(java.time.DayOfWeek.TUESDAY);
            ar1.setStartTime(java.time.LocalTime.of(14,0));
            ar1.setEndTime(java.time.LocalTime.of(19,0));
            
            AvailabilityRule ar2 = new AvailabilityRule();
            ar2.setTutorId(savedJonas.getId());
            ar2.setDayOfWeek(java.time.DayOfWeek.WEDNESDAY);
            ar2.setStartTime(java.time.LocalTime.of(14,0));
            ar2.setEndTime(java.time.LocalTime.of(19,0));
            
            availabilityRuleRepository.saveAll(List.of(ar1, ar2));

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
private void seedUsers(UserRepository userRepository) {
    upsertUser(userRepository, STUDENT_SUB, "Thani", "thanhhiendang521@gmail.com", Role.STUDENT);
    upsertUser(userRepository, TUTOR_SUB,   "Thani", "thanhhiendang521@yahoo.de",  Role.TUTOR);
    upsertUser(userRepository, ADMIN_SUB,   "Jarmila", "j.dauth@outlook.com",      Role.ADMIN);
    upsertUser(userRepository, "auth0|69600b3a6f4f6b2870b06d21",   "Thamila", "dieuhienmy@yahoo.de", Role.ADMIN);
}

private void upsertUser(UserRepository userRepository,
                        String oauthId,
                        String name,
                        String email,
                        Role role) {

    User u = userRepository.findByOauthId(oauthId).orElseGet(User::new);

    u.setOauthId(oauthId);
    u.setName(name);
    u.setEmail(email);
    u.setRole(role);

    userRepository.save(u);
}

}