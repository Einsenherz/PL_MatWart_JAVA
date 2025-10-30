package com.example.myapp.model;

public class Benutzer {
    private String username;
    private String password;
    private boolean admin;

    public Benutzer() {}

    public Benutzer(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }

    public String[] toCsv() {
        return new String[]{username, password, String.valueOf(admin)};
    }

    public static Benutzer fromCsv(String[] data) {
        return new Benutzer(data[0], data[1], Boolean.parseBoolean(data[2]));
    }
}
