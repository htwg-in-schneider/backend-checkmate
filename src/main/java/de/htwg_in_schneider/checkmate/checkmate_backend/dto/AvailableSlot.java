package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import java.time.LocalDateTime;

public class AvailableSlot {
    public LocalDateTime startAt;
    public Integer durationMinutes;

    public AvailableSlot(LocalDateTime startAt, Integer durationMinutes) {
        this.startAt = startAt;
        this.durationMinutes = durationMinutes;
    }
}