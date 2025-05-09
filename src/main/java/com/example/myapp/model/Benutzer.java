package com.example.myapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Benutzer {

    @Id
    private String username;
    private String passwort;

    public Benutzer() {}

    public Benutzer(String username, String passwort) {
        this.username = username;
        this.passwort = passwort;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }
}
