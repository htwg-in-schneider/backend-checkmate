package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings",
       indexes = {
           @Index(name = "idx_booking_tutor_start", columnList = "tutorId,startAt")
       })
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tutorId;

    private String tutorName;

    // Auth0 sub vom Student, der gebucht hat
    private String studentOauthId;

    private String studentName;

    private LocalDateTime startAt;

    private Integer durationMinutes;

    private Double price;

    @Column(length = 2000)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
    private Long transactionId;

    public Long getId() { return id; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getStudentOauthId() { return studentOauthId; }
    public void setStudentOauthId(String studentOauthId) { this.studentOauthId = studentOauthId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() {
      return startAt.plusMinutes(durationMinutes);
  }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    

public Long getTransactionId() { return transactionId; }
public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
}