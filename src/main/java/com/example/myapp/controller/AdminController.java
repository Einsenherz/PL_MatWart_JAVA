package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
        return "<script>window.location.href='/'</script>";
    }

    @GetMapping("")
    public String adminPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        return """
            <html><head><title>Admin</title></head><body>
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

        List<String> benutzerList = service.getAlleBenutzerNamen();
        StringBuilder html = new StringBuilder();
        html.append("<h1>Benutzer-Logins</h1>");
        for (String benutzer : benutzerList) {
            html.append("<div>").append(benutzer).append("</div>");
        }
        return html.toString();
    }

    @GetMapping("/listen")
    public String listenPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        List<String> benutzerList = service.getAlleBenutzerNamen();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(service.getZone());
        StringBuilder html = new StringBuilder("<h1>Alle Bestellungen</h1>");
        for (String benutzer : benutzerList) {
            List<Bestellung> bestellungen = service.getBestellungen(benutzer).stream()
                    .filter(b -> !"Archiviert".equals(b.getStatus()))
                    .toList();
            html.append("<h2>").append(benutzer).append("</h2><table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
            for (Bestellung b : bestellungen) {
                html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>")
                        .append(b.getMaterial()).append("</td><td>")
                        .append(b.getStatus()).append("</td><td>")
                        .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td><td>")
                        .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td></tr>");
            }
            html.append("</table>");
        }
        return html.toString();
    }

    @GetMapping("/archiv")
    public String archivPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(service.getZone());
        List<Bestellung> archiviert = service.getAlleArchiviertenBestellungenSorted();
        StringBuilder html = new StringBuilder("<h1>Archiv</h1><table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
        for (Bestellung b : archiviert) {
            html.append("<tr><td>").append(b.getBenutzer()).append("</td><td>")
                    .append(b.getAnzahl()).append("</td><td>")
                    .append(b.getMaterial()).append("</td><td>")
                    .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td><td>")
                    .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td></tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    @PostMapping("/listen/update")
    public String updateStatus(@RequestParam MultiValueMap<String, String> params, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        for (String key : params.keySet()) {
            if (key.startsWith("status_")) {
                Long id = Long.valueOf(key.substring(7));
                String status = params.getFirst(key);
                service.updateStatusMitRueckgabe(id, status);
            }
        }
        return "<script>window.location.href='/admin/listen';</script>";
    }
}

