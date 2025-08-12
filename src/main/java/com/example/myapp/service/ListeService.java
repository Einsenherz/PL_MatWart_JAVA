package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import com.example.myapp.repository.MaterialRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListeService {

    private final BenutzerRepository benutzerRepository;
    private final BestellungRepository bestellungRepository;
    private final MaterialRepository materialRepository;

    public ListeService(BenutzerRepository benutzerRepository,
                        BestellungRepository bestellungRepository,
                        MaterialRepository materialRepository) {
        this.benutzerRepository = benutzerRepository;
        this.bestellungRepository = bestellungRepository;
        this.materialRepository = materialRepository;
    }

    // ===== Benutzer =====
    public List<Benutzer> getAlleBenutzer() {
        return benutzerRepository.findAll();
    }

    public void addBenutzer(String username, String passwort) {
        benutzerRepository.save(new Benutzer(username, passwort));
    }

    public void updateBenutzer(String oldUsername, String newUsername, String newPasswort) {
        Benutzer b = benutzerRepository.findByUsername(oldUsername);
        if (b != null) {
            b.setUsername(newUsername);
            b.setPasswort(newPasswort);
            benutzerRepository.save(b);
        }
    }

    public void deleteBenutzer(String username) {
        Benutzer b = benutzerRepository.findByUsername(username);
        if (b != null) benutzerRepository.delete(b);
    }

    // ===== Bestellungen =====
    public List<Bestellung> getAlleBestellungen() {
        return bestellungRepository.findAll();
    }

    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        return bestellungRepository.findByStatusOrderByEingabedatumDesc("Archiviert");
    }

    @Transactional
    public void updateStatusMitBestand(Long id, String neuerStatus) {
        Bestellung b = bestellungRepository.findById(id).orElse(null);
        if (b != null) {
            String alterStatus = b.getStatus();

            // Status setzen
            b.setStatus(neuerStatus);

            // Wenn Archiviert => Bestand erhöhen & Rueckgabedatum setzen
            if ("Archiviert".equals(neuerStatus) && !"Archiviert".equals(alterStatus)) {
                Material m = materialRepository.findByName(b.getMaterial());
                if (m != null) {
                    m.setBestand(m.getBestand() + b.getAnzahl());
                    materialRepository.save(m);
                }
                b.setRueckgabedatum(LocalDateTime.now());
            }

            // Keine Bestandsänderung bei "Rückgabe fällig" oder "Bestätigt"
            bestellungRepository.save(b);
        }
    }

    public void leereArchiv() {
        List<Bestellung> archiv = getAlleArchiviertenBestellungenSorted();
        bestellungRepository.deleteAll(archiv);
    }

    // ===== Inventar =====
    public List<Material> getAlleMaterialien() {
        return materialRepository.findAll();
    }

    public void addMaterial(String name, int bestand) {
        if (materialRepository.findByName(name) == null) {
            materialRepository.save(new Material(name, bestand));
        }
    }

    @PostConstruct
    public void initInventar() {
        if (materialRepository.count() == 0) {
            // Beispiel-Startdaten
            materialRepository.save(new Material("Hammer", 10));
            materialRepository.save(new Material("Schraubenzieher", 15));
            materialRepository.save(new Material("Bohrmaschine", 5));
        }
    }
}
