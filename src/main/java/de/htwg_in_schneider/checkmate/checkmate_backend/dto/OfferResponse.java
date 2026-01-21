package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import java.time.Instant;

public class OfferResponse {
    public String ownerName;
    public String ownerEmail;
    public Long id;
    public Long tutorId;   
    public String subject;
    public Integer semester;
    public Integer hourlyRate;
    public Instant createdAt;
}