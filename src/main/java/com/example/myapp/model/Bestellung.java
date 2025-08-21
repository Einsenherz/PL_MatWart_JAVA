package com.example.myapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bestellung")
public class Bestellung {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String benutzer;
    private String material;
    private int anzahl;
    private String status = "in Bearbeitung";

    private LocalDateTime eingabedatum = LocalDateTime.now();
    private LocalDateTime rueckgabedatum;

    public Long getId() { return id; }
    public String getBenutzer() { return benutzer; }
    public void setBenutzer(String benutzer) { this.benutzer = benutzer; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public int getAnzahl() { return anzahl; }
    public void setAnzahl(int anzahl) { this.anzahl = anzahl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getEingabedatum() { return eingabedatum; }
    public void setEingabedatum(LocalDateTime eingabedatum) { this.eingabedatum = eingabedatum; }
    public LocalDateTime getRueckgabedatum() { return rueckgabedatum; }
    public void setRueckgabedatum(LocalDateTime rueckgabedatum) { this.rueckgabedatum = rueckgabedatum; }
}
