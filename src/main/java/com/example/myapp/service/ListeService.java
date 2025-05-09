package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListeService {
    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;

    public ListeService(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    // Gibt die Zeitzone zurück
    public ZoneId getZone() {
        return ZoneId.of("Europe/Berlin");
    }

    // Überprüft Passwort
    public boolean checkPasswort(String username, String passwort) {
        return benutzerRepo.findById(username)
                .map(b -> b.getPasswort().equals(passwort))
                .orElse(false);
    }

    // Benutzer hinzufügen
    public void addBenutzer(String username, String passwort) {
        benutzerRepo.save(new Benutzer(username, passwort));
    }

    // Alle Benutzernamen zurückgeben
    public List<String> getAlleBenutzerNamen() {
        return benutzerRepo.findAll().stream()
                .map(Benutzer::getUsername)
                .collect(Collectors.toList());
    }

    // Bestellungen eines Benutzers holen
    public List<Bestellung> getBestellungen(String benutzer) {
        return bestellungRepo.findByBenutzer(benutzer);
    }

    // Bestellen
    public void bestelle(String benutzer, int anzahl, String material) {
        Bestellung bestellung = new Bestellung(benutzer, anzahl, material, "in Bearbeitung");
        bestellungRepo.save(bestellung);
    }

    // Status aktualisieren
    public void updateStatus(Long id, String status) {
        bestellungRepo.findById(id).ifPresent(b -> {
            b.setStatus(status);
            bestellungRepo.save(b);
        });
    }

    // Status aktualisieren und Rückgabedatum setzen, falls Archiviert
    public void updateStatusMitRueckgabe(Long id, String status) {
        bestellungRepo.findById(id).ifPresent(b -> {
            b.setStatus(status);
            if ("Archiviert".equals(status)) {
                b.setRueckgabedatum(LocalDateTime.now(getZone()));
            }
            bestellungRepo.save(b);
        });
    }

    // Benutzer löschen + Bestellungen
    public void deleteBenutzer(String username) {
        benutzerRepo.deleteById(username);
        bestellungRepo.findByBenutzer(username).forEach(bestellungRepo::delete);
    }

    // Als abgegeben markieren (setzt Eingabedatum beim ersten Mal)
    public void markiereAlsAbgegeben(String benutzer) {
        List<Bestellung> liste = bestellungRepo.findByBenutzer(benutzer);
        for (Bestellung b : liste) {
            if (b.getEingabedatum() == null) {  // nur einmal
                b.setEingabedatum(LocalDateTime.now(getZone()));
                bestellungRepo.save(b);
            }
        }
    }

    // Alle archivierten Bestellungen sortiert zurückgeben
    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        return bestellungRepo.findAll().stream()
                .filter(b -> "Archiviert".equals(b.getStatus()))
                .sorted(Comparator
                        .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bestellung::getBenutzer))
                .collect(Collectors.toList());
    }

    // Einzelne Bestellung löschen
    public void deleteBestellung(Long id) {
        bestellungRepo.deleteById(id);
    }
}
