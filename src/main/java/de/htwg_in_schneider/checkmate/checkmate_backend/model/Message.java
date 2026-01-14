package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tutorId;

    @Column(nullable = false, length = 200)
    private String studentOauthId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sender sender;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum Sender { STUDENT, TUTOR }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public String getStudentOauthId() { return studentOauthId; }
    public void setStudentOauthId(String studentOauthId) { this.studentOauthId = studentOauthId; }

    public Sender getSender() { return sender; }
    public void setSender(Sender sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}