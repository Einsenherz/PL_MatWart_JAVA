package com.example.myapp.integration;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Material;
import com.example.myapp.model.Bestellung;
import java.util.List;

public class SpeicherListe {
    private List<Benutzer> benutzer;
    private List<Material> materialien;
    private List<Bestellung> bestellungen;

    public List<Benutzer> getBenutzer() { return benutzer; }
    public void setBenutzer(List<Benutzer> benutzer) { this.benutzer = benutzer; }

    public List<Material> getMaterialien() { return materialien; }
    public void setMaterialien(List<Material> materialien) { this.materialien = materialien; }

    public List<Bestellung> getBestellungen() { return bestellungen; }
    public void setBestellungen(List<Bestellung> bestellungen) { this.bestellungen = bestellungen; }
}
