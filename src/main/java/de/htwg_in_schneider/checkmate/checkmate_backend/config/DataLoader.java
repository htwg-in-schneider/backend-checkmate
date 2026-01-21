package de.htwg_in_schneider.checkmate.checkmate_backend.config;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.*;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DataLoader.class);

    // ✅ Auth0 "sub" IDs
    private static final String STUDENT_SUB = "auth0|695e5f38bd9509a108b5604d";
    private static final String TUTOR_SUB   = "auth0|695e66fcd58fa9152ab1d6f8";
    private static final String ADMIN_SUB   = "auth0|695fda2b6f4f6b2870b04cbd";

    @Bean
    public CommandLineRunner loadData(
            TutorRepository tutorRepository,
            AvailabilityRuleRepository availabilityRuleRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            PlatformTransactionManager txManager
    ) {

        TransactionTemplate tx = new TransactionTemplate(txManager);

        return args -> tx.execute(status -> {

            // ---- always seed users/students (upsert) ----
            seedUsers(userRepository);
            seedStudents(userRepository, studentRepository);

            // ✅ IMPORTANT: Always try to link ownerSub for existing tutors
            linkTutorOwnerSubs(tutorRepository);

            // ---- only seed demo tutors/reviews if empty ----
            if (tutorRepository.count() > 0) {
                LOG.info("Database already contains tutors. Skipping initial tutor/review data load.");
                return null;
            }

            LOG.info("Database empty. Loading initial tutor data…");

            // ---------- Tutor:innen anlegen ----------
            Tutor lisa = new Tutor();
            lisa.setName("Lisa Weber");
            lisa.setSubject("Mathe I");
            lisa.setSemester(5);
            lisa.setImage("https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=500&q=60");
            lisa.setCategory(Category.MATHE1);
            lisa.setHourlyRate(25.0);
            lisa.setEmail("lisa@outlook.com");

            Tutor jonas = new Tutor();
            jonas.setName("Jonas Keller");
            jonas.setSubject("Programmierung mit Java");
            jonas.setSemester(3);
            jonas.setImage("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=500&q=60");
            jonas.setCategory(Category.PROGRAMMIEREN);
            jonas.setHourlyRate(20.0);
            jonas.setEmail("jonas@outlook.com");

            Tutor mia = new Tutor();
            mia.setName("Mia Hoffmann");
            mia.setSubject("BWL Grundlagen");
            mia.setSemester(4);
            mia.setImage("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80");
            mia.setCategory(Category.BWL1);
            mia.setHourlyRate(22.0);
            mia.setEmail("mia@outlook.com");

            // In DB speichern – IDs werden von JPA vergeben
            List<Tutor> savedTutors = tutorRepository.saveAll(Arrays.asList(lisa, jonas, mia));
            LOG.info("Saved {} tutors.", savedTutors.size());

            Tutor savedLisa  = savedTutors.get(0);
            Tutor savedJonas = savedTutors.get(1);
            Tutor savedMia   = savedTutors.get(2);

            // ✅ Now that tutors exist, we can link ownerSub (again)
            linkTutorOwnerSubs(tutorRepository);

            // ---------- AvailabilityRules ----------
            // Jonas: Di + Mi 14–19
            AvailabilityRule j1 = new AvailabilityRule();
            j1.setTutorId(savedJonas.getId());
            j1.setDayOfWeek(DayOfWeek.TUESDAY);
            j1.setStartTime(LocalTime.of(14, 0));
            j1.setEndTime(LocalTime.of(19, 0));

            AvailabilityRule j2 = new AvailabilityRule();
            j2.setTutorId(savedJonas.getId());
            j2.setDayOfWeek(DayOfWeek.WEDNESDAY);
            j2.setStartTime(LocalTime.of(14, 0));
            j2.setEndTime(LocalTime.of(19, 0));

            // Lisa: Freitag 10–16
            AvailabilityRule l1 = new AvailabilityRule();
            l1.setTutorId(savedLisa.getId());
            l1.setDayOfWeek(DayOfWeek.FRIDAY);
            l1.setStartTime(LocalTime.of(10, 0));
            l1.setEndTime(LocalTime.of(16, 0));

            // Mia: Sonntag + Montag 12–18
            AvailabilityRule m1 = new AvailabilityRule();
            m1.setTutorId(savedMia.getId());
            m1.setDayOfWeek(DayOfWeek.SUNDAY);
            m1.setStartTime(LocalTime.of(12, 0));
            m1.setEndTime(LocalTime.of(18, 0));

            AvailabilityRule m2 = new AvailabilityRule();
            m2.setTutorId(savedMia.getId());
            m2.setDayOfWeek(DayOfWeek.MONDAY);
            m2.setStartTime(LocalTime.of(12, 0));
            m2.setEndTime(LocalTime.of(18, 0));

            availabilityRuleRepository.saveAll(List.of(j1, j2, l1, m1, m2));

            // ---------- Reviews ----------
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
            LOG.info("Initial tutor + availability + review data loaded successfully.");

            return null;
        });
    }

    /**
     * Link existing Tutor rows to Auth0 ownerSub.
     * This makes tutorRepository.findByOwnerSub(ownerSub) work,
     * which is needed for Offers + Messages mapping.
     *
     * NOTE: This only links the demo tutor (Jonas) to TUTOR_SUB.
     * Add more mappings if you have more tutor auth accounts.
     */
    private void linkTutorOwnerSubs(TutorRepository tutorRepository) {
        // Example mapping: the Auth0 tutor account (TUTOR_SUB) corresponds to demo tutor Jonas.
        final String tutorEmailForAuthAccount = "jonas@outlook.com";

        tutorRepository.findAll().forEach(t -> {
            if (t.getOwnerSub() != null && !t.getOwnerSub().isBlank()) return;
            if (t.getEmail() == null || t.getEmail().isBlank()) return;

            if (t.getEmail().equalsIgnoreCase(tutorEmailForAuthAccount)) {
                t.setOwnerSub(TUTOR_SUB);
                tutorRepository.save(t);
                LOG.info("Linked Tutor '{}' ({}) to ownerSub {}", t.getName(), t.getEmail(), TUTOR_SUB);
            }
        });
    }

    private void seedUsers(UserRepository userRepository) {
        upsertUser(userRepository, STUDENT_SUB, "Thani", "thanhhiendang521@gmail.com", Role.STUDENT);
        upsertUser(userRepository, TUTOR_SUB,   "Thani", "thanhhiendang521@yahoo.de",  Role.TUTOR);
        upsertUser(userRepository, ADMIN_SUB,   "Jarmila", "j.dauth@outlook.com",      Role.ADMIN);
        upsertUser(userRepository, "auth0|69600b3a6f4f6b2870b06d21", "Thamila", "dieuhienmy@yahoo.de", Role.ADMIN);

        // Fake Students nur für DB
        upsertUser(userRepository, "auth0|seed-student-1", "Stella Beckham", "stella@student.de", Role.STUDENT);
        upsertUser(userRepository, "auth0|seed-student-2", "Nico Freund", "nico@student.de", Role.STUDENT);
        upsertUser(userRepository, "auth0|seed-student-3", "Chris Bergmann", "chris@student.de", Role.STUDENT);
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
                STUDENT_SUB,
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

        Student s = studentRepository.findById(user.getId()).orElseGet(Student::new);

        s.setUser(managed);
        s.setAboutMe(aboutMe);
        s.setFieldOfStudy(fieldOfStudy);
        s.setSubjects(new ArrayList<>(subject));
        s.setSemester(semester);
        s.setUniversity(university);
        s.setImageUrl(imageUrl);

        studentRepository.save(s);
    }
}