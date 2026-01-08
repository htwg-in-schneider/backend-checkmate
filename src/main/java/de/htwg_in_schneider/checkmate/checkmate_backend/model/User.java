package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user") // "user" ist oft reserviert in SQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Auth0 "sub" (z.B. auth0|695e5...)
    @Column(unique = true, nullable = false)
    private String oauthId;

    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- getters/setters ---
    public Long getId() { return id; }

    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
    