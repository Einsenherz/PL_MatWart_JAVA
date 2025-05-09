package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import org.springframework.web.bind.annotation.*;

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
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Admin</title><style>")
            .append("body { text-align: center; font-family: Arial; }")
            .append("</style></head><body>");
        html.append("<h1>Admin-Bereich</h1>");
        html.append("<a href='/admin/logins'>Benutzerverwaltung</a><br>");
        html.append("<a href='/admin/listen'>Bestellungen</a><br>");
        html.append("<a href='/admin/archiv'>Archiv</a><br>");
        html.append("<br><a href='/'>Logout</a>");
        html.append("</body></html>");
        return html.toString();
    }

    @GetMapping("/logins")
    public String benutzerListe() {
        List<Benutzer> benutzer = service.getAlleBenutzer();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Benutzerverwaltung</title><style>")
            .append("body { text-align: center; font-family: Arial; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("th, td { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body>");
        html.append("<h1>Benutzerverwaltung</h1>");
        html.append("<table><tr><th>Benutzername</th></tr>");
        for (Benutzer b : benutzer) {
            html.append("<tr><td>").append(b.getUsername()).append("</td></tr>");
        }
        html.append("</table>");
        html.append("<br><a href='/admin'>Zurück</a>");
        html.append("</body></html>");
        return html.toString();
    }

    @GetMapping("/listen")
    public String bestellListe() {
        List<Bestellung> bestellungen = service.getBestellungen(""); // alle Bestellungen
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestellungen</title><style>")
            .append("body { text-align: center; font-family: Arial; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("th, td { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body>");
        html.append("<h1>Bestellungen</h1>");
        html.append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                .append("<td>").append(b.getBenutzer()).append("</td>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td>").append(b.getStatus()).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("</tr>");
        }
        html.append("</table>");
        html.append("<br><a href='/admin'>Zurück</a>");
        html.append("</body></html>");
        return html.toString();
    }

    @GetMapping("/archiv")
    public String archivListe() {
        List<Bestellung> archiv = service.getAlleArchiviertenBestellungen();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Archiv</title><style>")
            .append("body { text-align: center; font-family: Arial; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("th, td { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body>");
        html.append("<h1>Archiv</h1>");
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
        html.append("<br><a href='/admin'>Zurück</a>");
        html.append("</body></html>");
        return html.toString();
    }
}
