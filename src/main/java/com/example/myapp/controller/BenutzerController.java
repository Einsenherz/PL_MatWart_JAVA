package com.example.myapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "benutzer", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class Benutzer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwort;

    @Column(nullable = false)
    private String rolle = "USER";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswort() { return passwort; }
    public void setPasswort(String passwort) { this.passwort = passwort; }
    public String getRolle() { return rolle; }
    public void setRolle(String rolle) { this.rolle = rolle; }
}
