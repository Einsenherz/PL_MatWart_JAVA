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
import java.util.stream.Collectors;

@Service
public class ListeService {
    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;
    private final ZoneId zone = ZoneId.of("Europe/Zurich"); // DE/CH Zeitzone

    public ListeService(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    // Login check
    public boolean checkPasswort(String username, String passwort) {
        return benutzerRepo.findById(username)
                .map(b -> b.getPasswort().equals(passwort))
                .orElse(false);
    }

    // Benutzer hinzufügen
    public void addBenutzer(String username, String passwort) {
        benutzerRepo.save(new Benutzer(username, passwort));
    }

    // Alle Benutzer als Liste von Objekten
    public List<Benutzer> getAlleBenutzer() {
        return benutzerRepo.findAll();
    }

    // Alle Benutzernamen (nur Namen, für AdminController)
    public List<String> getAlleBenutzerNamen() {
        return benutzerRepo.findAll().stream()
                .map(Benutzer::getUsername)
                .collect(Collectors.toList());
    }

    // Benutzer löschen + Bestellungen löschen
    public void deleteBenutzer(String username) {
        benutzerRepo.deleteById(username);
        bestellungRepo.deleteAll(bestellungRepo.findByBenutzer(username));
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

    // Bestellung per ID finden
    public Optional<Bestellung> findBestellungById(Long id) {
        return bestellungRepo.findById(id);
    }

    // Bestellung erstellen
    public void bestelle(String benutzer, int anzahl, String material) {
        Bestellung b = new Bestellung(benutzer, anzahl, material, "in Bearbeitung");
        bestellungRepo.save(b);
    }

    // Status aktualisieren + Rückgabedatum bei Archiviert setzen
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

    // ➤ NEU: Benutzerseite generieren (HTML)
    public String generiereBenutzerSeite(String benutzer) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Benutzerseite</title></head><body>");
        html.append("<h1>Willkommen, ").append(benutzer).append("</h1>");
        html.append("<ul>");
        for (Bestellung b : getBestellungen(benutzer)) {
            html.append("<li>").append(b.getMaterial()).append(" (").append(b.getAnzahl()).append(")</li>");
        }
        html.append("</ul></body></html>");
        return html.toString();
    }

    // ➤ NEU: Bestellung löschen, wenn Status in Bearbeitung + kein Eingabedatum
    public void loescheBestellungWennMoeglich(Long id) {
        Bestellung b = bestellungRepo.findById(id).orElse(null);
        if (b != null && "in Bearbeitung".equalsIgnoreCase(b.getStatus()) && b.getEingabedatum() == null) {
            bestellungRepo.delete(b);
        }
    }

    // ZoneId getter
    public ZoneId getZone() {
        return zone;
    }
}
