package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ListeService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

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
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestelllisten</title><style>")
            .append("body { font-family: Arial; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body><h1>Alle Bestelllisten</h1>");

        service.getAlleBenutzer().forEach(benutzer -> {
            List<Bestellung> liste = service.getBestellungen(benutzer).stream()
                    .filter(b -> !"Archiviert".equals(b.getStatus()))
                    .sorted(Comparator.comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Bestellung::getMaterial))
                    .toList();
            html.append("<h2>").append(benutzer).append("</h2>")
                .append("<form action='/admin/listen/update/").append(benutzer).append("' method='post'>")
                .append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
            for (Bestellung b : liste) {
                html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>")
                    .append(b.getMaterial()).append("</td><td>")
                    .append("<select name='status").append(b.getId()).append("'>");
                for (String status : List.of("in Bearbeitung", "bestätigt", "Rückgabe erforderlich", "Archiviert")) {
                    html.append("<option")
                        .append(status.equals(b.getStatus()) ? " selected" : "")
                        .append(">").append(status).append("</option>");
                }
                html.append("</select></td><td>")
                    .append(b.getEingabedatum() != null ? b.getEingabedatum().format(formatter) : "")
                    .append("</td><td>")
                    .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(formatter) : "")
                    .append("</td></tr>");
            }
            html.append("</table><button type='submit'>MatWart OK!</button></form>");
        });

        html.append("<br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }

    @PostMapping("/listen/update/{benutzer}")
    public String updateListen(@PathVariable String benutzer, @RequestParam MultiValueMap<String, String> params, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        service.getBestellungen(benutzer).forEach(b -> {
            String newStatus = params.getFirst("status" + b.getId());
            if (newStatus != null && !"Archiviert".equals(b.getStatus()) && "Archiviert".equals(newStatus)) {
                b.setRueckgabedatum(java.time.LocalDateTime.now());
            }
            if (newStatus != null) {
                b.setStatus(newStatus);
            }
            service.saveBestellung(b);
        });
        return "<script>window.location.href='/admin/listen';</script>";
    }

    @GetMapping("/archiv")
    public String archivPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        List<Bestellung> archiv = service.getAlleBestellungen().stream()
                .filter(b -> "Archiviert".equals(b.getStatus()))
                .sorted(Comparator.comparing(Bestellung::getBenutzer)
                        .thenComparing(Bestellung::getEingabedatum))
                .toList();

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Archiv</title><style>")
            .append("body { font-family: Arial; } table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body><h1>Archivierte Bestellungen</h1>")
            .append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
        for (Bestellung b : archiv) {
            html.append("<tr><td>").append(b.getBenutzer()).append("</td><td>")
                .append(b.getAnzahl()).append("</td><td>")
                .append(b.getMaterial()).append("</td><td>")
                .append(b.getStatus()).append("</td><td>")
                .append(b.getEingabedatum() != null ? b.getEingabedatum().format(formatter) : "")
                .append("</td><td>")
                .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(formatter) : "")
                .append("</td></tr>");
        }
        html.append("</table><br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }
}
