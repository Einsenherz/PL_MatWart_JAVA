package com.example.myapp.model;

public class Bestellung {
    private String benutzername;
    private String material;
    private int menge;
    private String status; // z. B. "Offen", "In Bearbeitung", "Erledigt"

    public Bestellung() {}

    public Bestellung(String benutzername, String material, int menge, String status) {
        this.benutzername = benutzername;
        this.material = material;
        this.menge = menge;
        this.status = status;
    }

    public String getBenutzername() { return benutzername; }
    public void setBenutzername(String benutzername) { this.benutzername = benutzername; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public int getMenge() { return menge; }
    public void setMenge(int menge) { this.menge = menge; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return benutzername + ";" + material + ";" + menge + ";" + status;
    }
}
