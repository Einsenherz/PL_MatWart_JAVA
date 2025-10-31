package com.example.myapp.model;

public class Benutzer {
    private String username;
    private String password;
    private boolean admin;

    public Benutzer(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    public static Benutzer fromCsv(String[] arr) {
        return new Benutzer(arr[0], arr[1], Boolean.parseBoolean(arr[2]));
    }

    public String[] toCsv() {
        return new String[]{username, password, String.valueOf(admin)};
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAdmin() { return admin; }

    @Override
    public String toString() {
        return username + (admin ? " (Admin)" : "");
    }
}