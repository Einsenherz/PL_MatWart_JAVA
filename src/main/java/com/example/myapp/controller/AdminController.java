package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ListeService service;

    public AdminController(ListeService service) {
        this.service = service;
    }

    @GetMapping
    public String adminHome() {
        return "<html><head><title>Admin</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "</head><body>"
                + "<header><h1>Admin-Bereich</h1></header>"
                + "<main>"
                + "<form method='get' action='/admin/logins'><button type='submit'>Benutzerverwaltung</button></form>"
                + "<form method='get' action='/admin/listen'><button type='submit'>Bestellungen</button></form>"
                + "<form method='get' action='/admin/archiv'><button type='submit'>Archiv</button></form>"
                + "<form method='get' action='/'><button class='btn-back' type='submit'>Logout</button></form>"
                + "</main></body></html>";
    }

    @GetMapping("/logins")
    public String benutzerListe() {
        List<Benutzer> benutzer = service.getAlleBenutzer();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Benutzerverwaltung</title><link rel='stylesheet' href='/style.css'></head><body>");
        html.append("<header><h1>Benutzerverwaltung</h1></header><main>");
        html.append("<table><tr><th>Benutzername</th><th>Passwort</th><th>Aktionen</th></tr>");
        for (Benutzer b : benutzer) {
            html.append("<tr><td>").append(b.getUsername()).append("</td>")
                .append("<td>").append(b.getPasswort()).append("</td><td>")
                .append("<form style='display:inline;' method='get' action='/admin/logins/anpassen'>")
                .append("<input type='hidden' name='username' value='").append(b.getUsername()).append("'>")
                .append("<button type='submit'>Anpassen</button></form>")
                .append("<form style='display:inline;' method='post' action='/admin/logins/loeschen' onsubmit='return confirm(\"Benutzer wirklich löschen?\");'>")
                .append("<input type='hidden' name='username' value='").append(b.getUsername()).append("'>")
                .append("<button type='submit'>Löschen</button></form>")
                .append("</td></tr>");
        }
        html.append("</table><h2>Neuen Benutzer hinzufügen</h2>");
        html.append("<form method='post' action='/admin/logins/add'>")
            .append("Benutzername: <input type='text' name='username' required><br>")
            .append("Passwort: <input type='password' name='passwort' required><br>")
            .append("<button type='submit'>Hinzufügen</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main></body></html>");
        return html.toString();
    }

    @PostMapping("/logins/add")
    public String addBenutzer(@RequestParam String username, @RequestParam String passwort) {
        service.addBenutzer(username, passwort);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @GetMapping("/logins/anpassen")
    public String anpassenForm(@RequestParam String username) {
        return "<html><head><title>Benutzer anpassen</title><link rel='stylesheet' href='/style.css'></head><body>"
                + "<header><h1>Benutzer anpassen: " + username + "</h1></header><main>"
                + "<form method='post' action='/admin/logins/anpassen'>"
                + "<input type='hidden' name='oldUsername' value='" + username + "'>"
                + "Neuer Benutzername: <input type='text' name='newUsername' value='" + username + "' required><br>"
                + "Neues Passwort: <input type='password' name='newPasswort' required><br>"
                + "<button type='submit'>Bestätigen</button></form>"
                + "<form method='get' action='/admin/logins'><button class='btn-back' type='submit'>Abbrechen</button></form>"
                + "</main></body></html>";
    }

    @PostMapping("/logins/anpassen")
    public String updateBenutzer(@RequestParam String oldUsername,
                                 @RequestParam String newUsername,
                                 @RequestParam String newPasswort) {
        service.updateBenutzer(oldUsername, newUsername, newPasswort);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/loeschen")
    public String loescheBenutzer(@RequestParam String username) {
        service.deleteBenutzer(username);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @GetMapping("/listen")
    public String bestellListe() {
        List<Bestellung> bestellungen = service.getAlleBestellungen().stream()
            .filter(b -> !"Archiviert".equals(b.getStatus()))
            .toList();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestellungen</title><link rel='stylesheet' href='/style.css'></head><body>");
        html.append("<header><h1>Bestellungen</h1></header><main>");
        html.append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th><th>Aktionen</th></tr>");
        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                .append("<td>").append(b.getBenutzer()).append("</td>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td><form method='post' action='/admin/listen/status'>")
                .append("<input type='hidden' name='id' value='").append(b.getId()).append("'>")
                .append("<select name='status'>")
                .append("<option value='in Bearbeitung'" + ("in Bearbeitung".equals(b.getStatus()) ? " selected" : "") + ">in Bearbeitung</option>")
                .append("<option value='Bestätigt'" + ("Bestätigt".equals(b.getStatus()) ? " selected" : "") + ">Bestätigt</option>")
                .append("<option value='Rückgabe fällig'" + ("Rückgabe fällig".equals(b.getStatus()) ? " selected" : "") + ">Rückgabe fällig</option>")
                .append("</select><button type='submit'>Ändern</button></form></td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("<td><form method='post' action='/admin/listen/archivieren'>")
                .append("<input type='hidden' name='id' value='").append(b.getId()).append("'>")
                .append("<button type='submit'>Archivieren</button></form></td>")
                .append("</tr>");
        }
        html.append("</table><form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main></body></html>");
        return html.toString();
    }

    @GetMapping("/archiv")
    public String archivListe() {
        List<Bestellung> archiv = service.getAlleArchiviertenBestellungenSorted();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Archiv</title><link rel='stylesheet' href='/style.css'></head><body>");
        html.append("<header><h1>Archiv</h1></header><main>");
        html.append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
        for (Bestellung b : archiv) {
            html.append("<tr>")
                .append("<td>").append(b.getBenutzer()).append("</td>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("</tr>");
        }
        html.append("</table>");
        html.append("<form method='get' action='/admin/archiv/export' style='display:inline;'><button type='submit'>📁 Archiv exportieren</button></form>");
        html.append("<form method='post' action='/admin/archiv/clear' style='display:inline;' onsubmit='return confirm(\"Wirklich alle archivierten Einträge löschen?\");'><button type='submit'>🗑️ Archiv leeren</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main></body></html>");
        return html.toString();
    }

    @GetMapping("/archiv/export")
    public ResponseEntity<byte[]> exportiereArchivAlsCsv() {
        List<Bestellung> archiv = service.getAlleArchiviertenBestellungenSorted();
        StringBuilder csv = new StringBuilder("Benutzer,Anzahl,Material,Eingabedatum,Rueckgabedatum\n");
        for (Bestellung b : archiv) {
            csv.append(b.getBenutzer()).append(',')
               .append(b.getAnzahl()).append(',')
               .append(b.getMaterial()).append(',')
               .append(b.getEingabedatum() != null ? b.getEingabedatum() : "").append(',')
               .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum() : "")
               .append('\n');
        }

        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archiv.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvBytes);
    }

    @PostMapping("/archiv/clear")
    public String archivLeeren() {
        service.leereArchiv();
        return "<script>window.location.href='/admin/archiv';</script>";
    }

    @PostMapping("/listen/status")
    public String updateStatus(@RequestParam Long id, @RequestParam String status) {
        service.updateStatusMitRueckgabe(id, status);
        return "<script>window.location.href='/admin/listen';</script>";
    }

    @PostMapping("/listen/archivieren")
    public String archivieren(@RequestParam Long id) {
        service.updateStatusMitRueckgabe(id, "Archiviert");
        return "<script>window.location.href='/admin/listen';</script>";
    }
}
