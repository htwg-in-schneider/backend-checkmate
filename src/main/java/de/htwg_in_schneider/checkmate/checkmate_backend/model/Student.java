package de.htwg_in_schneider.checkmate.checkmate_backend.model;

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
    private String subject;       // z.B. Datenbanken
    private Integer semester;
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

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
}
