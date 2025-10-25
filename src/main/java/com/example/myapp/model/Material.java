package com.example.myapp.model;

public class Material {
    private long id;
    private String name;
    private int bestand;

    public Material(long id, String name, int bestand) {
        this.id = id;
        this.name = name;
        this.bestand = bestand;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBestand() {
        return bestand;
    }

    public void setBestand(int bestand) {
        this.bestand = bestand;
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bestand=" + bestand +
                '}';
    }
}
