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
import java.util.Optional;

@Service
public class ListeService {
    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;
    private final ZoneId zone = ZoneId.of("Europe/Zurich"); // Zeitzone

    public ListeService(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    // Passwort prüfen
    public boolean checkPasswort(String username, String passwort) {
        return benutzerRepo.findById(username)
                .map(b -> b.getPasswort().equals(passwort))
                .orElse(false);
    }

    // Benutzer hinzufügen
    public void addBenutzer(String username, String passwort) {
        benutzerRepo.save(new Benutzer(username, passwort));
    }

    // Benutzer löschen
    public void deleteBenutzer(String username) {
        benutzerRepo.deleteById(username);
    }

    // Benutzer aktualisieren
    public void updateBenutzer(String oldUsername, String newUsername, String newPasswort) {
        Optional<Benutzer> opt = benutzerRepo.findById(oldUsername);
        if (opt.isPresent()) {
            Benutzer benutzer = opt.get();
            benutzerRepo.delete(benutzer);
            benutzerRepo.save(new Benutzer(newUsername, newPasswort));
        }
    }

    // Alle Benutzer
    public List<Benutzer> getAlleBenutzer() {
        return benutzerRepo.findAll();
    }

    // Alle Bestellungen
    public List<Bestellung> getAlleBestellungen() {
        return bestellungRepo.findAll();
    }

    // Bestellungen eines Benutzers
    public List<Bestellung> getBestellungen(String benutzer) {
        return bestellungRepo.findByBenutzer(benutzer)
                .stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .toList();
    }

    // Archivierte Bestellungen
    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        return bestellungRepo.findAll()
                .stream()
                .filter(b -> "Archiviert".equals(b.getStatus()))
                .sorted(Comparator
                        .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    // Bestellung speichern
    public void saveBestellung(Bestellung b) {
        bestellungRepo.save(b);
    }

    // Bestellung finden
    public Optional<Bestellung> findBestellungById(Long id) {
        return bestellungRepo.findById(id);
    }

    // Bestellung erstellen
    public void bestelle(String benutzer, int anzahl, String material) {
        Bestellung b = new Bestellung(benutzer, anzahl, material, "in Bearbeitung");
        bestellungRepo.save(b);
    }

    // Status aktualisieren inkl. Rückgabedatum
    public void updateStatusMitRueckgabe(Long id, String status) {
        bestellungRepo.findById(id).ifPresent(b -> {
            b.setStatus(status);
            if ("Archiviert".equals(status) && b.getRueckgabedatum() == null) {
                b.setRueckgabedatum(LocalDateTime.now(zone));
            }
            bestellungRepo.save(b);
        });
    }

    // Eingabedatum setzen beim "An MatWart senden"
    public void markiereAlsAbgegeben(String benutzer) {
        List<Bestellung> liste = bestellungRepo.findByBenutzer(benutzer);
        for (Bestellung b : liste) {
            if (b.getEingabedatum() == null) {
                b.setEingabedatum(LocalDateTime.now(zone));
                bestellungRepo.save(b);
            }
        }
    }

    // Bestellung nur löschen, wenn Status "in Bearbeitung" und kein Eingabedatum gesetzt
    public boolean loescheBestellungWennMoeglich(Long id) {
        return bestellungRepo.findById(id).map(b -> {
            if ("in Bearbeitung".equals(b.getStatus()) && b.getEingabedatum() == null) {
                bestellungRepo.delete(b);
                return true;
            }
            return false;
        }).orElse(false);
    }


    public ZoneId getZone() {
        return zone;
    }
}
