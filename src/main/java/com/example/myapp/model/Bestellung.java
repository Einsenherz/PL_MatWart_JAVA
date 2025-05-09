package com.example.myapp.model;

public class Bestellung {
    private int anzahl;
    private String material;
    private String status;

    public Bestellung(int anzahl, String material, String status) {
        this.anzahl = anzahl;
        this.material = material;
        this.status = status;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public String getMaterial() {
        return material;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
