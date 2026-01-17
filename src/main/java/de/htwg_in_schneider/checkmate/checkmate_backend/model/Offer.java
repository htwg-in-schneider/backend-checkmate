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
    private String title;

    @Column(nullable = false)
    private String subject;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Integer hourlyRate;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column
    private String location;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.durationMinutes == null) this.durationMinutes = 60;
        if (this.hourlyRate == null) this.hourlyRate = 20;
        if (this.location == null) this.location = "Online";
    }

    // ===== Getters/Setters =====

    public Long getId() { return id; }

    public String getOwnerSub() { return ownerSub; }
    public void setOwnerSub(String ownerSub) { this.ownerSub = ownerSub; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Integer hourlyRate) { this.hourlyRate = hourlyRate; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getCreatedAt() { return createdAt; }
}