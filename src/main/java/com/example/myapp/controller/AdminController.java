package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Benutzer;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;

    public AdminController(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    private boolean isAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute("loggedInUser"));
    }

    private String redirectToLogin() {
        return "<script>window.location.href='/'</script>";
    }

    @GetMapping("")
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
    public String loginsPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Logins verwalten</title><style>")
            .append("body { text-align: center; font-family: Arial; margin-top: 30px; }")
            .append("input, button { font-size: 14px; margin: 5px; }")
            .append("</style></head><body><h1>Benutzer-Logins</h1>");

        List<Benutzer> alleBenutzer = benutzerRepo.findAll();
        for (Benutzer benutzer : alleBenutzer) {
            html.append("<form action='/admin/logins/update/").append(benutzer.getUsername()).append("' method='post'>")
                .append("<input type='text' name='name' value='").append(benutzer.getUsername()).append("' readonly>")
                .append("<input type='text' name='passwort' value='").append(benutzer.getPasswort()).append("' readonly>")
                .append("<button type='submit' formaction='/admin/logins/edit/").append(benutzer.getUsername()).append("'>Anpassen</button>")
                .append("<button type='submit' formaction='/admin/logins/delete/").append(benutzer.getUsername()).append("'>Löschen</button>")
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
        benutzerRepo.save(new Benutzer(name, passwort));
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/delete/{benutzer}")
    public String deleteLogin(@PathVariable String benutzer, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        benutzerRepo.deleteById(benutzer);
        bestellungRepo.findByBenutzer(benutzer).forEach(bestellungRepo::delete);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/update/{benutzer}")
    public String updateLogin(@PathVariable String benutzer, @RequestParam String name, @RequestParam String passwort, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        Benutzer b = benutzerRepo.findById(benutzer).orElse(null);
        if (b != null) {
            b.setUsername(name);
            b.setPasswort(passwort);
            benutzerRepo.save(b);
        }
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @GetMapping("/listen")
    public String listenPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestelllisten</title><style>")
            .append("body { font-family: Arial; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body><h1>Alle Bestelllisten</h1>");

        List<Benutzer> alleBenutzer = benutzerRepo.findAll();
        for (Benutzer benutzer : alleBenutzer) {
            List<Bestellung> bestellungen = bestellungRepo.findByBenutzer(benutzer.getUsername()).stream()
                .sorted(Comparator
                    .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

            html.append("<h2>").append(benutzer.getUsername()).append("</h2>")
                .append("<form action='/admin/listen/update/").append(benutzer.getUsername()).append("' method='post'>")
                .append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");

            for (Bestellung b : bestellungen) {
                html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>")
                    .append(b.getMaterial()).append("</td><td>")
                    .append("<select name='status_").append(b.getId()).append("'>")
                    .append("<option ").append("in Bearbeitung".equals(b.getStatus()) ? "selected" : "").append(">in Bearbeitung</option>")
                    .append("<option ").append("Bestätigt".equals(b.getStatus()) ? "selected" : "").append(">Bestätigt</option>")
                    .append("<option ").append("Rückgabe erforderlich".equals(b.getStatus()) ? "selected" : "").append(">Rückgabe erforderlich</option>")
                    .append("<option ").append("Archiviert".equals(b.getStatus()) ? "selected" : "").append(">Archiviert</option>")
                    .append("</select></td><td>")
                    .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "")
                    .append("</td><td>")
                    .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "")
                    .append("</td></tr>");
            }

            html.append("</table><button type='submit'>MatWart OK!</button></form>");
        }

        html.append("<br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }

    @PostMapping("/listen/update/{benutzer}")
    public String updateListen(@PathVariable String benutzer, @RequestParam Map<String, String> allParams, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        for (String key : allParams.keySet()) {
            if (key.startsWith("status_")) {
                Long id = Long.parseLong(key.substring(7));
                Bestellung b = bestellungRepo.findById(id).orElse(null);
                if (b != null) {
                    String newStatus = allParams.get(key);
                    if (!"Archiviert".equals(b.getStatus()) && "Archiviert".equals(newStatus)) {
                        b.setRueckgabedatum(java.time.LocalDateTime.now());
                    }
                    b.setStatus(newStatus);
                    bestellungRepo.save(b);
                }
            }
        }
        return "<script>window.location.href='/admin/listen';</script>";
    }

    @GetMapping("/archiv")
    public String archivPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Archiv</title><style>")
            .append("body { font-family: Arial; } table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body><h1>Archivierte Bestellungen</h1>")
            .append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");

        bestellungRepo.findAll().stream()
            .filter(b -> "Archiviert".equals(b.getStatus()))
            .sorted(Comparator
                .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
            .forEach(b -> html.append("<tr><td>").append(b.getBenutzer()).append("</td><td>")
                .append(b.getAnzahl()).append("</td><td>").append(b.getMaterial()).append("</td><td>")
                .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "")
                .append("</td><td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "")
                .append("</td></tr>"));

        html.append("</table><br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }
}
