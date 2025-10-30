package com.example.myapp.model;

public class Bestellung {
    private int id;
    private String benutzer;
    private String material;
    private int anzahl;
    private String status;

    public Bestellung() {}

    public Bestellung(int id, String benutzer, String material, int anzahl, String status) {
        this.id = id;
        this.benutzer = benutzer;
        this.material = material;
        this.anzahl = anzahl;
        this.status = status;
    }

    public int getId() { return id; }
    public String getBenutzer() { return benutzer; }
    public String getMaterial() { return material; }
    public int getAnzahl() { return anzahl; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setBenutzer(String benutzer) { this.benutzer = benutzer; }
    public void setMaterial(String material) { this.material = material; }
    public void setAnzahl(int anzahl) { this.anzahl = anzahl; }
    public void setStatus(String status) { this.status = status; }

    public String[] toCsv() {
        return new String[]{String.valueOf(id), benutzer, material, String.valueOf(anzahl), status};
    }

    public static Bestellung fromCsv(String[] d) {
        return new Bestellung(Integer.parseInt(d[0]), d[1], d[2], Integer.parseInt(d[3]), d[4]);
    }
}

