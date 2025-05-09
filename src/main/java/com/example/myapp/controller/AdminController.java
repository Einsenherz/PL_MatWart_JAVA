package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
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

        List<String> benutzerListe = service.getAlleBenutzer();
        for (String benutzer : benutzerListe) {
            List<Bestellung> bestellungen = service.getBestellungen(benutzer).stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .sorted((b1, b2) -> {
                    if (b1.getEingabedatum() == null && b2.getEingabedatum() != null) return 1;
                    if (b1.getEingabedatum() != null && b2.getEingabedatum() == null) return -1;
                    if (b1.getEingabedatum() == null) return b1.getMaterial().compareTo(b2.getMaterial());
                    int cmp = b1.getEingabedatum().compareTo(b2.getEingabedatum());
                    return cmp != 0 ? cmp : b1.getMaterial().compareTo(b2.getMaterial());
                })
                .collect(Collectors.toList());

            html.append("<h2>").append(benutzer).append("</h2>")
                .append("<form action='/admin/listen/update/").append(benutzer).append("' method='post'>")
                .append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
            for (Bestellung b : bestellungen) {
                html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>")
                    .append(b.getMaterial()).append("</td><td>")
                    .append("<select name='status").append(b.getId()).append("'>")
                    .append("<option").append(b.getStatus().equals("in Bearbeitung") ? " selected" : "").append(">in Bearbeitung</option>")
                    .append("<option").append(b.getStatus().equals("Bestätigt") ? " selected" : "").append(">Bestätigt</option>")
                    .append("<option").append(b.getStatus().equals("Rückgabe erforderlich") ? " selected" : "").append(">Rückgabe erforderlich</option>")
                    .append("<option").append(b.getStatus().equals("Archiviert") ? " selected" : "").append(">Archiviert</option>")
                    .append("</select></td><td>")
                    .append(b.getEingabedatum() != null ? b.getEingabedatum().atZone(service.getZone()).format(dtf) : "")
                    .append("</td><td>")
                    .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().atZone(service.getZone()).format(dtf) : "")
                    .append("</td></tr>");
            }
            html.append("</table><button type='submit'>MatWart OK!</button></form>");
        }

        html.append("<br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }

    @PostMapping("/listen/update/{benutzer}")
    public String updateListen(@PathVariable String benutzer, @RequestParam MultiValueMap<String, String> allParams, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();

        for (String key : allParams.keySet()) {
            if (key.startsWith("status")) {
                Long id = Long.parseLong(key.substring(6));
                String newStatus = allParams.getFirst(key);
                service.updateStatusMitRueckgabe(id, newStatus);
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
            .append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");

        List<Bestellung> archivierte = service.getAlleArchiviertenBestellungenSorted();
        for (Bestellung b : archivierte) {
            html.append("<tr><td>").append(b.getBenutzer()).append("</td><td>")
                .append(b.getAnzahl()).append("</td><td>")
                .append(b.getMaterial()).append("</td><td>")
                .append(b.getStatus()).append("</td><td>")
                .append(b.getEingabedatum() != null ? b.getEingabedatum().atZone(service.getZone()).format(dtf) : "")
                .append("</td><td>")
                .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().atZone(service.getZone()).format(dtf) : "")
                .append("</td></tr>");
        }
        html.append("</table><br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }
}
