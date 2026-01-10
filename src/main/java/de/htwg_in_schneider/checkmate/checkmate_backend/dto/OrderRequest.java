package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

public class OrderRequest {

    public Long tutorId;
    public String tutorName;

    public Integer durationMinutes; // z.B. 60
    public Double price;            // z.B. 25.0

    public String note;             // optional
}