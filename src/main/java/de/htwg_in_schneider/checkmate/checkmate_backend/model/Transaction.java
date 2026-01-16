package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_oauth_id", nullable = false)
    private String studentOauthId;

    private String studentName;
    private String buyerEmail;

    @Column(length = 2000)
    private String note;

    private double totalPrice;

    private LocalDateTime createdAt = LocalDateTime.now();

    // --- getters/setters ---

    public Long getId() { return id; }

    public String getStudentOauthId() { return studentOauthId; }
    public void setStudentOauthId(String studentOauthId) { this.studentOauthId = studentOauthId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}