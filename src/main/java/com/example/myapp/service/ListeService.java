package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    public boolean checkPasswort(String username, String passwort) {
        return benutzerRepo.findById(username)
                .map(b -> b.getPasswort().equals(passwort))
                .orElse(false);
    }

    public void addBenutzer(String username, String passwort) {
        benutzerRepo.save(new Benutzer(username, passwort));
    }

    public List<Bestellung> getBestellungen(String benutzer) {
        return bestellungRepo.findByBenutzer(benutzer);
    }

    public List<Benutzer> getAlleBenutzer() {
        return benutzerRepo.findAll();
    }

    public void updateBenutzer(String oldUsername, String newUsername, String passwort) {
        benutzerRepo.deleteById(oldUsername);
        benutzerRepo.save(new Benutzer(newUsername, passwort));
    }

    public void bestelle(String benutzer, int anzahl, String material) {
        Bestellung bestellung = new Bestellung(benutzer, anzahl, material, "in Bearbeitung");
        bestellungRepo.save(bestellung);
    }

    public void updateStatus(Long id, String status) {
        bestellungRepo.findById(id).ifPresent(b -> {
            b.setStatus(status);
            if ("Archiviert".equals(status) && b.getRueckgabedatum() == null) {
                b.setRueckgabedatum(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
            }
            bestellungRepo.save(b);
        });
    }

    public void deleteBenutzer(String username) {
        benutzerRepo.deleteById(username);
        bestellungRepo.findByBenutzer(username).forEach(bestellungRepo::delete);
    }

    public void markiereAlsAbgegeben(String benutzer) {
        List<Bestellung> liste = bestellungRepo.findByBenutzer(benutzer);
        for (Bestellung b : liste) {
            if (b.getEingabedatum() == null) {
                b.setEingabedatum(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                bestellungRepo.save(b);
            }
        }
    }

    public List<Bestellung> getAlleBestellungen() {
        return bestellungRepo.findAll();
    }

    public String generiereBenutzerSeite(String benutzer) {
        List<Bestellung> bestellungen = getBestellungen(benutzer).stream()
                .sorted(Comparator.comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

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
                .append("<h2>Bestellliste:</h2><table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");

        for (Bestellung b : bestellungen) {
            html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>")
                    .append(b.getMaterial()).append("</td><td>")
                    .append(b.getStatus()).append("</td><td>")
                    .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "")
                    .append("</td><td>")
                    .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "")
                    .append("</td></tr>");
        }

        html.append("</table><br><form action='/normalbenutzer/").append(benutzer).append("/senden' method='post'>")
                .append("<button type='submit'>An MatWart senden!</button></form>")
                .append("<br><a href='/'>Logout</a></body></html>");

        return html.toString();
    }
}
