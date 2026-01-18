package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "direct_messages")
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String senderOauthId;

    @Column(nullable = false, length = 200)
    private String receiverOauthId;

    @Column(nullable = false, length = 200)
    private String senderName;

    @Column(nullable = false, length = 2000)
    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getSenderOauthId() { return senderOauthId; }
    public void setSenderOauthId(String senderOauthId) { this.senderOauthId = senderOauthId; }

    public String getReceiverOauthId() { return receiverOauthId; }
    public void setReceiverOauthId(String receiverOauthId) { this.receiverOauthId = receiverOauthId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}