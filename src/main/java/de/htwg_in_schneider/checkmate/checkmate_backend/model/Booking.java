package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tutorId", "startAt"})
  }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tutorId;

    private LocalDateTime startAt;

    private Integer durationMinutes;

    // optional
    private String studentName;
    private String note;

    // getters/setters
    public Long getId() { return id; }

    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}