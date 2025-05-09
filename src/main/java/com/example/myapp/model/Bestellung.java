package com.example.myapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    // Getters & Setters (wie schon bekannt)
    // [hier wie im alten Code]
}
