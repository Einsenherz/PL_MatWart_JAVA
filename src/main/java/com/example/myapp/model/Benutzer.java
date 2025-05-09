package com.example.myapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Benutzer {
    @Id
    private String name;
    private String passwort;

    public Benutzer() {}

    public Benutzer(String name, String passwort) {
        this.name = name;
        this.passwort = passwort;
    }

    // Getter + Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPasswort() { return passwort; }
    public void setPasswort(String passwort) { this.passwort = passwort; }
}
