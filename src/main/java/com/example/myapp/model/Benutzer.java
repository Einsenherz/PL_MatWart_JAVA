package com.example.myapp.model;

public class Benutzer {
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    @Override
    public String toString() {
        return username + ";" + passwort;
    }
}
