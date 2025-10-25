package com.example.myapp.model;

import java.time.LocalDateTime;

public class Bestellung {
    private long id;
    private String benutzer;
    private String material;
    private int anzahl;
    private LocalDateTime eingabedatum;
    private LocalDateTime rueckgabedatum;

    public Bestellung(long id, String benutzer, String material, int anzahl,
                      LocalDateTime eingabedatum, LocalDateTime rueckgabedatum) {
        this.id = id;
        this.benutzer = benutzer;
        this.material = material;
        this.anzahl = anzahl;
        this.eingabedatum = eingabedatum;
        this.rueckgabedatum = rueckgabedatum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBenutzer() {
        return benutzer;
    }

    public void setBenutzer(String benutzer) {
        this.benutzer = benutzer;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }

    public LocalDateTime getEingabedatum() {
        return eingabedatum;
    }

    public void setEingabedatum(LocalDateTime eingabedatum) {
        this.eingabedatum = eingabedatum;
    }

    public LocalDateTime getRueckgabedatum() {
        return rueckgabedatum;
    }

    public void setRueckgabedatum(LocalDateTime rueckgabedatum) {
        this.rueckgabedatum = rueckgabedatum;
    }

    @Override
    public String toString() {
        return "Bestellung{" +
                "id=" + id +
                ", benutzer='" + benutzer + '\'' +
                ", material='" + material + '\'' +
                ", anzahl=" + anzahl +
                ", eingabedatum=" + eingabedatum +
                ", rueckgabedatum=" + rueckgabedatum +
                '}';
    }
}
