package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class CreateTransactionItem {

    @NotNull(message = "tutorId darf nicht null sein")
    private Long tutorId;

    @NotBlank(message = "tutorName darf nicht leer sein")
    private String tutorName;

    @NotNull(message = "startAt darf nicht null sein")
    private LocalDateTime startAt;

    @NotNull(message = "durationMinutes darf nicht null sein")
    @Min(value = 30, message = "durationMinutes muss mindestens 30 sein")
    private Integer durationMinutes;

    @NotNull(message = "price darf nicht null sein")
    @DecimalMin(value = "0.01", message = "price muss > 0 sein")
    private Double price;

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
}