package com.example.myapp.model;

public class Benutzer {
    private String username;
    private String password;

    public Benutzer(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
