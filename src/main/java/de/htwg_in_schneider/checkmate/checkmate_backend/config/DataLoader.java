package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Review;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Tutor;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;

import de.htwg_in_schneider.checkmate.checkmate_backend.repository.ReviewRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TutorRepository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Student;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.StudentRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
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
                                      ReviewRepository reviewRepository,
                                      UserRepository userRepository,
                                      StudentRepository studentRepository,
                                      PlatformTransactionManager txManager) {

    TransactionTemplate tx = new TransactionTemplate(txManager);

    return args -> {
        tx.execute(status -> {
            seedUsers(userRepository);
            seedStudents(userRepository, studentRepository);

            if (tutorRepository.count() > 0) {
                LOG.info("Database already contains tutors. Skipping initial tutor/review data load.");
                return null;
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
            mia.setImage("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80");
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

            return null;
        });
    };
}
private void seedUsers(UserRepository userRepository) {
    upsertUser(userRepository, STUDENT_SUB, "Thani", "thanhhiendang521@gmail.com", Role.STUDENT);
    upsertUser(userRepository, TUTOR_SUB,   "Thani", "thanhhiendang521@yahoo.de",  Role.TUTOR);
    upsertUser(userRepository, ADMIN_SUB,   "Jarmila", "j.dauth@outlook.com",      Role.ADMIN);
    upsertUser(userRepository, "auth0|69600b3a6f4f6b2870b06d21",   "Thamila", "dieuhienmy@yahoo.de", Role.ADMIN);

    //Fake Students nur für DB 
    upsertUser(userRepository, "auth0|seed-student-1", "Stella Beckham", "stella@student.de", Role.STUDENT);
    upsertUser(userRepository, "auth0|seed-student-2", "Nico Freund",  "nico@student.de",  Role.STUDENT);
    upsertUser(userRepository, "auth0|seed-student-3", "Chris Bergmann","chris@student.de",Role.STUDENT);
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
private void seedStudents(UserRepository userRepository, StudentRepository studentRepository) {

    seedOneStudent(userRepository, studentRepository,
            "auth0|seed-student-1",
            "Ich bin Stella und suche Hilfe in DB.",
            "Informatik",
            List.of("Datenbanken"),
            3,
            "HTWG Konstanz",
            "https://plus.unsplash.com/premium_photo-1729581091962-8da050639694?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    );

    seedOneStudent(userRepository, studentRepository,
            "auth0|seed-student-2",
            "Nico hier – Mathe ist pain.",
            "Wirtschaftsinformatik",
            List.of("Mathe 1", "Datenbanken"),
            2,
            "HTWG Konstanz",
            "https://images.unsplash.com/photo-1520883491007-4920448f8310?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    );

    seedOneStudent(userRepository, studentRepository,
            "auth0|seed-student-3",
            "Chris – brauche Java Support.",
            "Informatik",
            List.of("Programmieren", "Mathe 1", "BWL"),
            1,
            "HTWG Konstanz",
            "https://images.unsplash.com/photo-1667285435776-baa546a57f87?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    );

        seedOneStudent(userRepository, studentRepository,
            "auth0|695e5f38bd9509a108b5604d",
            "Brauche Hilfe bei WebTech T-T",
            "Wirtschaftsinformatik",
            List.of("Web-Technologien"),
            7,
            "HTWG Konstanz",
            "https://plus.unsplash.com/premium_photo-1732757787045-d903f2e88b08?q=80&w=1354&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    );

    
}

private void seedOneStudent(UserRepository userRepository,
                            StudentRepository studentRepository,
                            String oauthId,
                            String aboutMe,
                            String fieldOfStudy,
                            List<String> subject,
                            Integer semester,
                            String university,
                            String imageUrl) {
User user = userRepository.findByOauthId(oauthId).orElseThrow();

    User managed = userRepository.getReferenceById(user.getId());

    Student s = studentRepository.findById(user.getId())
            .orElseGet(Student::new);

s.setUser(managed); // wichtig, falls neu
s.setAboutMe(aboutMe);
s.setFieldOfStudy(fieldOfStudy);
s.setSubjects(new ArrayList<>(subject));
s.setSemester(semester);
s.setUniversity(university);
s.setImageUrl(imageUrl);

studentRepository.save(s);
}
}