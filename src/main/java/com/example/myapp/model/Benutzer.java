package com.example.myapp.model;

import jakarta.persistence.*;

@Entity
public class Bestellung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String benutzer;
    private int anzahl;
    private String material;
    private String status;

    public Bestellung() {}

    public Bestellung(String benutzer, int anzahl, String material, String status) {
        this.benutzer = benutzer;
        this.anzahl = anzahl;
        this.material = material;
        this.status = status;
    }

    // Getter + Setter
    public Long getId() { return id; }
    public String getBenutzer() { return benutzer; }
    public void setBenutzer(String benutzer) { this.benutzer = benutzer; }
    public int getAnzahl() { return anzahl; }
    public void setAnzahl(int anzahl) { this.anzahl = anzahl; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
