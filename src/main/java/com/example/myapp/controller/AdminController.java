package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.time.LocalDateTime;

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
            .append("</style>")
            .append("<script>")
            .append("function toggleEdit(id) {")
            .append("  var nameField = document.getElementById('name-' + id);")
            .append("  var passField = document.getElementById('pass-' + id);")
            .append("  var btn = document.getElementById('btn-' + id);")
            .append("  if (btn.textContent === 'Anpassen') {")
            .append("    nameField.readOnly = false; passField.readOnly = false; btn.textContent = 'Bestätigen';")
            .append("  } else { if (confirm('Änderungen übernehmen?')) { document.getElementById('form-' + id).submit(); }")
            .append("else { nameField.readOnly = true; passField.readOnly = true; btn.textContent = 'Anpassen'; } }")
            .append("}")
            .append("</script></head><body>")
            .append("<h1>Benutzer-Logins</h1>");

        List<Benutzer> alleBenutzer = service.getAlleBenutzer();
        for (Benutzer b : alleBenutzer) {
            String username = b.getUsername();
            String passwort = b.getPasswort();
            html.append("<form id='form-").append(username).append("' action='/admin/logins/update/").append(username).append("' method='post'>")
                .append("<input type='text' id='name-").append(username).append("' name='name' value='").append(username).append("' readonly>")
                .append("<input type='text' id='pass-").append(username).append("' name='passwort' value='").append(passwort).append("' readonly>")
                .append("<button type='button' id='btn-").append(username).append("' onclick='toggleEdit(\"").append(username).append("\")'>Anpassen</button>")
                .append("<button type='submit' formaction='/admin/logins/delete/").append(username).append("'>Löschen</button>")
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
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/delete/{username}")
    public String deleteLogin(@PathVariable String username, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        service.deleteBenutzer(username);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/update/{username}")
    public String updateLogin(@PathVariable String username, @RequestParam String name, @RequestParam String passwort, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        service.updateBenutzer(username, name, passwort);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @GetMapping("/listen")
    public String listenPage(HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestelllisten</title><style>")
            .append("body { font-family: Arial; } table { margin: auto; border-collapse: collapse; } td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body><h1>Alle Bestelllisten</h1>");

        List<Benutzer> alleBenutzer = service.getAlleBenutzer();
        for (Benutzer b : alleBenutzer) {
            html.append("<h2>").append(b.getUsername()).append("</h2>");
            List<Bestellung> bestellungen = service.getBestellungen(b.getUsername()).stream()
                    .sorted(Comparator.comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Bestellung::getMaterial))
                    .toList();
            html.append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
            for (Bestellung best : bestellungen) {
                html.append("<tr><td>").append(best.getAnzahl()).append("</td><td>").append(best.getMaterial()).append("</td><td>")
                    .append(best.getStatus()).append("</td><td>").append(best.getEingabedatum()).append("</td><td>")
                    .append(best.getRueckgabedatum()).append("</td></tr>");
            }
            html.append("</table>");
        }

        html.append("<br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }
}
