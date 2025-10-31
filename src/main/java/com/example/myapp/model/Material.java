package com.example.myapp.model;

public class Material {
    private String name;
    private String beschreibung;
    private int bestand;

    public Material(String name, String beschreibung, int bestand) {
        this.name = name;
        this.beschreibung = beschreibung;
        this.bestand = bestand;
    }

    public static Material fromCsv(String[] arr) {
        String name = arr[0];
        String beschreibung = arr.length > 1 ? arr[1] : "";
        int bestand = arr.length > 2 ? Integer.parseInt(arr[2]) : 0;
        return new Material(name, beschreibung, bestand);
    }

    public String[] toCsv() {
        return new String[]{name, beschreibung, String.valueOf(bestand)};
    }

    public String getName() { return name; }
    public String getBeschreibung() { return beschreibung; }
    public int getBestand() { return bestand; }

    public void setBestand(int bestand) { this.bestand = bestand; }
}