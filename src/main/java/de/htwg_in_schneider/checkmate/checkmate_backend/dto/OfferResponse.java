package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import java.time.Instant;

public class OfferResponse {
    public Long id;
    public String title;
    public String subject;
    public String description;
    public Integer hourlyRate;
    public Integer durationMinutes;
    public String location;
    public Instant createdAt;
}