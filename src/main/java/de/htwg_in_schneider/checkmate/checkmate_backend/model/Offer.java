package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Auth0 user identifier (sub), z.B. "auth0|123..." oder "google-oauth2|..."
    @Column(nullable = false)
    private String ownerSub;

    @Column(nullable = false)
    private Long tutorId;


    @Column(nullable = false)
    private String subject;

    @Column
    private Integer semester;

    @Column(nullable = false)
    private Integer hourlyRate;


    @Column
    private String location;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.hourlyRate == null) this.hourlyRate = 20;
       
    }

    // ===== Getters/Setters =====

    public Long getId() { return id; }

    public String getOwnerSub() { return ownerSub; }
    public void setOwnerSub(String ownerSub) { this.ownerSub = ownerSub; }

    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }


    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Integer getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Integer hourlyRate) { this.hourlyRate = hourlyRate; }

    public Instant getCreatedAt() { return createdAt; }
}