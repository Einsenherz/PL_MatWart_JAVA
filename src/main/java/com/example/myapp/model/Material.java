package com.example.myapp.model;

public class Material {
    private int id;
    private String name;
    private int bestand;

    public Material() {}

    public Material(int id, String name, int bestand) {
        this.id = id;
        this.name = name;
        this.bestand = bestand;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getBestand() { return bestand; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBestand(int bestand) { this.bestand = bestand; }

    public String[] toCsv() {
        return new String[]{String.valueOf(id), name, String.valueOf(bestand)};
    }

    public static Material fromCsv(String[] data) {
        if (data == null || data.length < 3) {
            return new Material(0, "", 0);
        }
        return new Material(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]));
    }
}
