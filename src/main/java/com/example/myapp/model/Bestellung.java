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

    public long getId() { return id; }
    public String getBenutzer() { return benutzer; }
    public String getMaterial() { return material; }
    public int getAnzahl() { return anzahl; }
    public LocalDateTime getEingabedatum() { return eingabedatum; }
    public LocalDateTime getRueckgabedatum() { return rueckgabedatum; }
}
