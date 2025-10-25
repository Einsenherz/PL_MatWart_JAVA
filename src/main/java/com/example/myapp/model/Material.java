package com.example.myapp.model;

public class Material {
    private String name;
    private int anzahl;
    private String ort;

    public Material() {}

    public Material(String name, int anzahl, String ort) {
        this.name = name;
        this.anzahl = anzahl;
        this.ort = ort;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAnzahl() { return anzahl; }
    public void setAnzahl(int anzahl) { this.anzahl = anzahl; }

    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }

    @Override
    public String toString() {
        return name + ";" + anzahl + ";" + ort;
    }
}
