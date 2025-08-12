package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import com.example.myapp.repository.MaterialRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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

    // ===== Benutzer (ohne Admin in DB) =====
    public List<Benutzer> getAlleBenutzer() {
        // Admin wird nicht mit aufgelistet
        List<Benutzer> benutzer = benutzerRepository.findAll();
        benutzer.removeIf(b -> "admin".equalsIgnoreCase(b.getUsername()));
        return benutzer;
    }

    public void addBenutzer(String username, String passwort) {
        if (!"admin".equalsIgnoreCase(username)) {
            benutzerRepository.save(new Benutzer(username, passwort));
        }
    }

    public void updateBenutzer(String oldUsername, String newUsername, String newPasswort) {
        if ("admin".equalsIgnoreCase(oldUsername) || "admin".equalsIgnoreCase(newUsername)) {
            return; // Admin darf nicht geändert werden
        }
        Benutzer b = benutzerRepository.findByUsername(oldUsername);
        if (b != null) {
            b.setUsername(newUsername);
            b.setPasswort(newPasswort);
            benutzerRepository.save(b);
        }
    }

    public void deleteBenutzer(String username) {
        if ("admin".equalsIgnoreCase(username)) return; // Admin darf nicht gelöscht werden
        Benutzer b = benutzerRepository.findByUsername(username);
        if (b != null) benutzerRepository.delete(b);
    }

    // ===== Login =====
    public String checkLogin(String username, String passwort) {
        // Hardcodierter Admin
        if ("admin".equalsIgnoreCase(username) && "Dieros8500".equals(passwort)) {
            return "admin";
        }

        Benutzer benutzer = benutzerRepository.findByUsername(username);
        if (benutzer != null && benutzer.getPasswort().equals(passwort)) {
            return "benutzer";
        }
        return null;
    }

    // ===== Bestellungen =====
    public List<Bestellung> getAlleBestellungen() {
        return bestellungRepository.findAll();
    }

    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        return bestellungRepository.findByStatusOrderByEingabedatumDesc("Archiviert");
    }

    public List<Bestellung> getMeineBestellungen(String benutzername) {
        return bestellungRepository.findByBenutzerOrderByEingabedatumDesc(benutzername);
    }

    @Transactional
    public void addBestellung(String benutzer, String materialName, int anzahl) {
        Bestellung bestellung = new Bestellung();
        bestellung.setBenutzer(benutzer);
        bestellung.setMaterial(materialName);
        bestellung.setAnzahl(anzahl);
        bestellung.setStatus("in Bearbeitung");
        bestellung.setEingabedatum(LocalDateTime.now());
        bestellungRepository.save(bestellung);
    }

    @Transactional
    public void updateStatusMitBestand(Long id, String status) {
        Bestellung b = bestellungRepository.findById(id).orElse(null);
        if (b != null) {
            b.setStatus(status);
            bestellungRepository.save(b);

            // Bestände nur anpassen, wenn auf "Archiviert" gesetzt wird (egal welche Schreibweise)
            if ("archiviert".equalsIgnoreCase(status)) {
                Material m = materialRepository.findByName(b.getMaterial());
                if (m != null) {
                    m.setBestand(Math.max(0, m.getBestand() - b.getAnzahl()));
                    materialRepository.save(m);
                }
            }
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
            materialRepository.save(new Material("Hammer", 10));
            materialRepository.save(new Material("Schraubenzieher", 15));
            materialRepository.save(new Material("Bohrmaschine", 5));
        }
    }
}
