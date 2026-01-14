package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import java.time.LocalDateTime;

public class ChatThreadDto {
    public Long tutorId;
    public String tutorName;
    public String lastText;
    public LocalDateTime lastAt;

    public ChatThreadDto(Long tutorId, String tutorName, String lastText, LocalDateTime lastAt) {
        this.tutorId = tutorId;
        this.tutorName = tutorName;
        this.lastText = lastText;
        this.lastAt = lastAt;
    }
}