package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
public class Student{

    @Id
    private Long id; // gleiche ID wie user.id

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String aboutMe;

    private String fieldOfStudy;  // "Ich studiere"

    @ElementCollection
    @CollectionTable(
        name = "student_subjects", // So wird die neue Tabelle in DBeaver heißen
        joinColumns = @JoinColumn(name = "student_id") // So heißt die Verknüpfungs-Spalte in der neuen Tabelle
    )
    @Column(name = "subject")
    private List<String> subjects = new ArrayList<>();    private Integer semester;

    private String imageUrl;
    private String university;

    // getters/setters
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }

    public String getFieldOfStudy() { return fieldOfStudy; }
    public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
}
