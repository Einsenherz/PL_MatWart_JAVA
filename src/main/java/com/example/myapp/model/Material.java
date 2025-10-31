package com.example.myapp.model;

public class Material {
    private int id;
    private String name;
    private String beschreibung;
    private int bestand;

    public Material(int id, String name, String beschreibung, int bestand) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
        this.bestand = bestand;
    }

    public static Material fromCsv(String[] arr) {
        int id = 0;
        String name;
        String beschreibung = "";
        int bestand = 0;

        try {
            // Prüfe, ob die CSV mit ID beginnt
            if (arr.length >= 4) {
                id = Integer.parseInt(arr[0]);
                name = arr[1];
                beschreibung = arr[2];
                bestand = Integer.parseInt(arr[3]);
            } else {
                name = arr[0];
                if (arr.length > 1) beschreibung = arr[1];
                if (arr.length > 2) bestand = Integer.parseInt(arr[2]);
            }
        } catch (Exception e) {
            name = "?";
        }

        return new Material(id, name, beschreibung, bestand);
    }

    public String[] toCsv() {
        return new String[]{
            String.valueOf(id),
            name,
            beschreibung,
            String.valueOf(bestand)
        };
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBeschreibung() { return beschreibung; }
    public int getBestand() { return bestand; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBeschreibung(String beschreibung) { this.beschreibung = beschreibung; }
    public void setBestand(int bestand) { this.bestand = bestand; }
}