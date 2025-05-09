package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ListeService service;

    public AdminController(ListeService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute("loggedInUser"));
    }

    private String redirectToLogin() {
        return "redirect:/";
    }

    @GetMapping("")
    @ResponseBody
    public String adminPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        return """
            <html><head><title>Admin</title><style>
            body { text-align: center; font-family: Arial; margin-top: 50px; }
            button { font-size: 16px; margin: 10px; }
            </style></head><body>
            <h1>Admin-Bereich</h1>
            <button onclick="window.location.href='/admin/logins'">Logins</button>
            <button onclick="window.location.href='/admin/listen'">Listen</button>
            <button onclick="window.location.href='/admin/archiv'">Archiv</button>
            <br><a href='/'>Logout</a>
            </body></html>""";
    }

    @GetMapping("/logins")
    @ResponseBody
    public String loginsPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Logins verwalten</title><style>")
            .append("body { text-align: center; font-family: Arial; margin-top: 30px; }")
            .append("input, button { font-size: 14px; margin: 5px; }")
            .append("</style></head><body>")
            .append("<h1>Benutzer-Logins</h1>");

        List<String> benutzerNamen = service.getAlleBenutzerNamen();
        for (String benutzer : benutzerNamen) {
            html.append("<form action='/admin/logins/update/").append(benutzer).append("' method='post'>")
                .append("<input type='text' name='name' value='").append(benutzer).append("' readonly>")
                .append("<button type='submit' formaction='/admin/logins/delete/").append(benutzer).append("'>Löschen</button>")
                .append("</form>");
        }

        html.append("<h2>Neues Login hinzufügen</h2>")
            .append("<form action='/admin/logins/add' method='post'>")
            .append("<input type='text' name='name' placeholder='Benutzername' required>")
            .append("<input type='text' name='passwort' placeholder='Passwort' required>")
            .append("<button type='submit'>Hinzufügen</button></form>")
            .append("<br><a href='/admin'>Zurück</a></body></html>");

        return html.toString();
    }

    @PostMapping("/logins/add")
    public String addLogin(@RequestParam String name, @RequestParam String passwort, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        service.addBenutzer(name, passwort);
        return "redirect:/admin/logins";
    }

    @PostMapping("/logins/delete/{benutzer}")
    public String deleteLogin(@PathVariable String benutzer, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        service.deleteBenutzer(benutzer);
        return "redirect:/admin/logins";
    }

    @GetMapping("/listen")
    @ResponseBody
    public String listenPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestelllisten</title><style>")
            .append("body { font-family: Arial; } table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }</style></head><body><h1>Bestellungen</h1>");

        List<String> benutzerNamen = service.getAlleBenutzerNamen();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(service.getZone());

        for (String benutzer : benutzerNamen) {
            List<Bestellung> bestellungen = service.getBestellungen(benutzer);
            bestellungen = bestellungen.stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .toList();

            if (!bestellungen.isEmpty()) {
                html.append("<h2>").append(benutzer).append("</h2>")
                    .append("<form action='/admin/listen/update/").append(benutzer).append("' method='post'>")
                    .append("<table><tr><th>ID</th><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th></tr>");
                for (Bestellung b : bestellungen) {
                    html.append("<tr><td>").append(b.getId()).append("</td><td>")
                        .append(b.getAnzahl()).append("</td><td>")
                        .append(b.getMaterial()).append("</td><td>")
                        .append("<select name='status_").append(b.getId()).append("'>")
                        .append("<option ").append(b.getStatus().equals("in Bearbeitung") ? "selected" : "").append(">in Bearbeitung</option>")
                        .append("<option ").append(b.getStatus().equals("Bestätigt") ? "selected" : "").append(">Bestätigt</option>")
                        .append("<option ").append(b.getStatus().equals("Rückgabe erforderlich") ? "selected" : "").append(">Rückgabe erforderlich</option>")
                        .append("<option ").append(b.getStatus().equals("Archiviert") ? "selected" : "").append(">Archiviert</option>")
                        .append("</select></td><td>")
                        .append(b.getEingabedatum() != null ? dtf.format(b.getEingabedatum()) : "")
                        .append("</td></tr>");
                }
                html.append("</table><button type='submit'>Speichern</button></form>");
            }
        }

        html.append("<br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }

    @PostMapping("/listen/update/{benutzer}")
    public String updateListen(@PathVariable String benutzer, @RequestParam Map<String, String> params, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        params.forEach((key, value) -> {
            if (key.startsWith("status_")) {
                Long id = Long.parseLong(key.substring(7));
                service.updateStatusMitRueckgabe(id, value);
            }
        });
        return "redirect:/admin/listen";
    }

    @GetMapping("/archiv")
    @ResponseBody
    public String archivPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Archiv</title><style>")
            .append("body { font-family: Arial; } table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }</style></head><body><h1>Archiv</h1>")
            .append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(service.getZone());

        List<Bestellung> archiviert = service.getAlleArchiviertenBestellungenSorted();
        for (Bestellung b : archiviert) {
            html.append("<tr><td>").append(b.getBenutzer()).append("</td><td>")
                .append(b.getAnzahl()).append("</td><td>")
                .append(b.getMaterial()).append("</td><td>")
                .append(b.getEingabedatum() != null ? dtf.format(b.getEingabedatum()) : "")
                .append("</td><td>")
                .append(b.getRueckgabedatum() != null ? dtf.format(b.getRueckgabedatum()) : "")
                .append("</td></tr>");
        }
        html.append("</table><br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }
}
