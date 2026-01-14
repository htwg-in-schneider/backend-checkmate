package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tutorId;

    // Auth0 Sub vom Student
    private String studentOauthId;

    // "STUDENT" oder "TUTOR"
    private String senderRole;

    @Column(length = 2000)
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage() {}

    // getters/setters
    public Long getId() { return id; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public String getStudentOauthId() { return studentOauthId; }
    public void setStudentOauthId(String studentOauthId) { this.studentOauthId = studentOauthId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}