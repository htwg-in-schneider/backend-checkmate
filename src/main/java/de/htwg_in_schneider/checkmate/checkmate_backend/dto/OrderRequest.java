package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull(message = "tutorId darf nicht leer sein")
    private Long tutorId;

    @NotBlank(message = "tutorName darf nicht leer sein")
    private String tutorName;

    @NotNull(message = "durationMinutes darf nicht leer sein")
    @Min(value = 30, message = "durationMinutes muss mindestens 30 sein")
    private Integer durationMinutes;

    @NotNull(message = "price darf nicht leer sein")
    @Min(value = 0, message = "price muss >= 0 sein")
    private Double price;

    private String note;

    @NotNull
    private String startAt; 

    // Getter / Setter
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStartAt() { return startAt; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
}