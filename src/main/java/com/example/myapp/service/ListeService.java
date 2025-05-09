package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ListeService {
    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;

    public ListeService(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    // Prüfen, ob Passwort korrekt ist
    public boolean checkPasswort(String username, String passwort) {
        return benutzerRepo.findById(username)
                .map(b -> b.getPasswort().equals(passwort))
                .orElse(false);
    }

    // Neuen Benutzer hinzufügen
    public void addBenutzer(String username, String passwort) {
        benutzerRepo.save(new Benutzer(username, passwort));
    }

    // Alle Benutzer holen
    public List<Benutzer> getAlleBenutzer() {
        return benutzerRepo.findAll();
    }

    // Bestellungen eines Benutzers holen (ohne Archivierte)
    public List<Bestellung> getBestellungen(String benutzer) {
        return bestellungRepo.findByBenutzer(benutzer).stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .sorted(Comparator
                        .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    // Archivierte Bestellungen holen
    public List<Bestellung> getArchivierteBestellungen() {
        return bestellungRepo.findAll().stream()
                .filter(b -> "Archiviert".equals(b.getStatus()))
                .sorted(Comparator
                        .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    // Bestellung speichern
    public void saveBestellung(Bestellung bestellung) {
        bestellungRepo.save(bestellung);
    }

    // Neue Bestellung erstellen
    public void bestelle(String benutzer, int anzahl, String material) {
        Bestellung bestellung = new Bestellung(benutzer, anzahl, material, "in Bearbeitung");
        bestellungRepo.save(bestellung);
    }

    // Status einer Bestellung aktualisieren
    public void updateStatus(Long id, String status) {
        Bestellung b = bestellungRepo.findById(id).orElse(null);
        if (b != null) {
            if ("Archiviert".equals(status)) {
                b.setRueckgabedatum(LocalDateTime.now());
            }
            b.setStatus(status);
            bestellungRepo.save(b);
        }
    }

    // Bestellung anhand ID holen
    public Bestellung findBestellungById(Long id) {
        return bestellungRepo.findById(id).orElse(null);
    }

    // Bestellung löschen
    public void deleteBestellung(Bestellung bestellung) {
        bestellungRepo.delete(bestellung);
    }

    // Benutzer löschen (optional: auch Bestellungen löschen)
    public void deleteBenutzer(String username) {
        benutzerRepo.deleteById(username);
        bestellungRepo.findByBenutzer(username).forEach(bestellungRepo::delete);
    }

    // Eingabedatum setzen (nur beim ersten Mal)
    public void markiereAlsAbgegeben(String benutzer) {
        List<Bestellung> liste = bestellungRepo.findByBenutzer(benutzer);
        for (Bestellung b : liste) {
            if (b.getEingabedatum() == null) {
                b.setEingabedatum(LocalDateTime.now());
                bestellungRepo.save(b);
            }
        }
    }

    // Benutzer aktualisieren
    public void updateBenutzer(String oldUsername, String newUsername, String newPasswort) {
        Benutzer benutzer = benutzerRepo.findById(oldUsername).orElse(null);
        if (benutzer != null) {
            benutzerRepo.deleteById(oldUsername);
            benutzerRepo.save(new Benutzer(newUsername, newPasswort));

            // Bestellungen auf neuen Namen übertragen
            List<Bestellung> bestellungen = bestellungRepo.findByBenutzer(oldUsername);
            for (Bestellung b : bestellungen) {
                b.setBenutzer(newUsername);
                bestellungRepo.save(b);
            }
        }
    }

    // Generiere HTML-Seite für Benutzer (mit Löschsymbol nur falls erlaubt)
    public String generiereBenutzerSeite(String benutzer) {
        List<Bestellung> bestellungen = getBestellungen(benutzer);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Benutzer</title><style>")
            .append("body { text-align: center; font-family: Arial; margin-top: 50px; }")
            .append("input, button { font-size: 16px; margin: 5px; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body>")
            .append("<h1>Willkommen, ").append(benutzer).append("!</h1>")
            .append("<form action='/normalbenutzer/").append(benutzer).append("/bestellen' method='post'>")
            .append("<input type='number' name='anzahl' min='1' placeholder='Anzahl' required>")
            .append("<input type='text' name='material' placeholder='Material' required>")
            .append("<button type='submit'>Bestätigen</button></form>")
            .append("<h2>Bestellliste:</h2><table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th><th>Aktion</th></tr>");

        for (Bestellung b : bestellungen) {
            html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>")
                .append(b.getMaterial()).append("</td><td>")
                .append(b.getStatus()).append("</td><td>")
                .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "")
                .append("</td><td>")
                .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "")
                .append("</td>");

            // Löschsymbol nur wenn status == in Bearbeitung && eingabedatum == null
            if ("in Bearbeitung".equals(b.getStatus()) && b.getEingabedatum() == null) {
                html.append("<td><form method='post' action='/normalbenutzer/")
                    .append(benutzer).append("/delete/").append(b.getId()).append("'>")
                    .append("<button type='submit'>🗑️</button></form></td>");
            } else {
                html.append("<td></td>");
            }
            html.append("</tr>");
        }

        html.append("</table><br><form action='/normalbenutzer/").append(benutzer).append("/senden' method='post'>")
            .append("<button type='submit'>An MatWart senden!</button></form>")
            .append("<br><a href='/'>Logout</a></body></html>");

        return html.toString();
    }
}
