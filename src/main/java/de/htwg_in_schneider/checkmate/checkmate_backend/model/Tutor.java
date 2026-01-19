package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutor") // optional, aber sauber
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerSub;

    @Column(unique = true)
    private String oauthId; 

    private String name;
    private String subject;
    private int semester;
    private String image;
    private Double hourlyRate;
    private String email;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Tutor() {}

    public Tutor(Long id, String name, String subject, int semester, String image, Double hourlyRate, String email) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.semester = semester;
        this.image = image;
        this.hourlyRate = hourlyRate;
        this.email = email;
    }

    // ===== Getter & Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerSub() {
        return ownerSub;
    }

    public void setOwnerSub(String ownerSub) {
        this.ownerSub = ownerSub;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        review.setTutor(this);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
        review.setTutor(null);
    }

    // equals / hashCode nur über id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tutor tutor = (Tutor) o;
        return id != null && id.equals(tutor.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Tutor{" +
                "id=" + id +
                ", ownerSub='" + ownerSub + '\'' +
                ", name='" + name + '\'' +
                ", subject='" + subject + '\'' +
                ", semester=" + semester +
                ", image='" + image + '\'' +
                ", category=" + category +
                ", hourlyRate=" + hourlyRate +
                ", email='" + email + '\'' +
                '}';
    }
}