package com.example.myapp.integration;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import java.util.ArrayList;
import java.util.List;

public class SpeicherListe {
    public List<Benutzer> benutzer = new ArrayList<>();
    public List<Bestellung> bestellungen = new ArrayList<>();
    public List<Material> material = new ArrayList<>();
}
