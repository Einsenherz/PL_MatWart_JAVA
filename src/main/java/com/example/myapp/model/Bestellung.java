package com.example.myapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
public class Bestellung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String benutzer;
    private int anzahl;
    private String material;
    private String status;
    private LocalDateTime eingabedatum;
    private LocalDateTime rueckgabedatum;

    public Bestellung() {}

    public Bestellung(String benutzer, int anzahl, String material, String status) {
        this.benutzer = benutzer;
        this.anzahl = anzahl;
        this.material = material;
        this.status = status;
    }

    // Getter und Setter ...

    public Long getId() {
        return id;
    }

    public String getBenutzer() {
        return benutzer;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public String getMaterial() {
        return material;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getEingabedatum() {
        return eingabedatum;
    }

    public LocalDateTime getRueckgabedatum() {
        return rueckgabedatum;
    }

    public void setBenutzer(String benutzer) {
        this.benutzer = benutzer;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEingabedatum(LocalDateTime eingabedatum) {
        this.eingabedatum = eingabedatum;
    }

    public void setRueckgabedatum(LocalDateTime rueckgabedatum) {
        this.rueckgabedatum = rueckgabedatum;
    }
}
