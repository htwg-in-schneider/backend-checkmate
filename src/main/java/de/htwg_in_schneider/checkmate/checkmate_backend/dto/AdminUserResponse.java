package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Role;

public class AdminUserResponse {

    private Long id;
    private String oauthId;
    private String name;
    private String email;
    private Role role;

    public AdminUserResponse() {}

    public AdminUserResponse(Long id, String oauthId, String name, String email, Role role) {
        this.id = id;
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getOauthId() { return oauthId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public void setId(Long id) { this.id = id; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
}