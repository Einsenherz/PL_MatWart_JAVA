package com.example.myapp.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Bestellung {
    private int id;
    private String benutzer;
    private String material;
    private int anzahl;
    private String status;
    private String eingabeZeit;
    private String updated;

    private static final ZoneId ZONE = ZoneId.of("Europe/Zurich");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public Bestellung(int id, String benutzer, String material, int anzahl, String status,
                      String eingabeZeit, String updated) {
        this.id = id;
        this.benutzer = benutzer;
        this.material = material;
        this.anzahl = anzahl;
        this.status = status;
        this.eingabeZeit = eingabeZeit;
        this.updated = updated;
    }

    // CSV → Bestellung
    public static Bestellung fromCsv(String[] arr) {
        try {
            int id = Integer.parseInt(arr[0]);
            String benutzer = arr[1];
            String material = arr[2];
            int anzahl = Integer.parseInt(arr[3]);
            String status = arr[4];
            String eingabe = arr.length > 5 ? arr[5] : FMT.format(LocalDateTime.now(ZONE));
            String updated = arr.length > 6 ? arr[6] : eingabe;
            return new Bestellung(id, benutzer, material, anzahl, status, eingabe, updated);
        } catch (Exception e) {
            return new Bestellung(0, "?", "?", 0, "Offen",
                    FMT.format(LocalDateTime.now(ZONE)),
                    FMT.format(LocalDateTime.now(ZONE)));
        }
    }

    // Bestellung → CSV
    public String[] toCsv() {
        return new String[]{
                String.valueOf(id),
                benutzer,
                material,
                String.valueOf(anzahl),
                status,
                eingabeZeit,
                updated
        };
    }

    // Getter
    public int getId() { return id; }
    public String getBenutzer() { return benutzer; }
    public String getMaterial() { return material; }
    public int getAnzahl() { return anzahl; }
    public String getStatus() { return status; }
    public String getEingabeZeit() { return eingabeZeit; }
    public String getUpdated() { return updated; }

    // Setter
    public void setStatus(String status) {
        this.status = status;
        this.updated = FMT.format(LocalDateTime.now(ZONE));
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}