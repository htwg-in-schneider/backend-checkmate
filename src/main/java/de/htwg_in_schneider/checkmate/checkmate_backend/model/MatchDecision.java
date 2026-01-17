package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import java.util.Optional;

import jakarta.persistence.*;


@Entity
@Table(
    name = "match_decision",
    uniqueConstraints = @UniqueConstraint(columnNames = {"from_user_id", "to_user_id"})
)
public class MatchDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    public enum Decision { LIKE, DISLIKE }

    // --- getters/setters ---
    public Long getId() { return id; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }



}
