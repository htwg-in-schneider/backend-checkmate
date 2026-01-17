package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class BookingRequest {

    @NotNull
    private Long tutorId;

    @NotBlank
    private String tutorName;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    @Min(30)
    @Max(240)
    private Integer durationMinutes;

    @NotNull
    @DecimalMin("0.0")
    private Double price;

    private String note;

    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}