package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ListeService service;

    public AdminController(ListeService service) {
        this.service = service;
    }

    @GetMapping("")
    public String adminPage() {
        return """
            <html><head><title>Admin</title><style>
            body { text-align: center; font-family: Arial; margin-top: 50px; }
            button { font-size: 16px; margin: 10px; }
            </style></head><body>
            <h1>Admin-Bereich</h1>
            <button onclick=\"window.location.href='/admin/logins'\">Logins</button>
            <button onclick=\"window.location.href='/admin/listen'\">Listen</button>
            <br><a href='/'>Logout</a>
            </body></html>""";
    }

    @GetMapping("/logins")
    public String loginsPage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Logins verwalten</title><style>")
            .append("body { text-align: center; font-family: Arial; margin-top: 30px; }")
            .append("input, button { font-size: 14px; margin: 5px; }")
            .append("</style></head><body>")
            .append("<h1>Benutzer-Logins</h1>");

        for (String benutzer : service.benutzerLogins.keySet()) {
            String passwort = service.benutzerLogins.get(benutzer);
            html.append("<form action='/admin/logins/update/" + benutzer + "' method='post'>")
                .append("<input type='text' name='name' value='" + benutzer + "' readonly>")
                .append("<input type='text' name='passwort' value='" + passwort + "' readonly>")
                .append("<button type='submit' formaction='/admin/logins/edit/" + benutzer + "'>Anpassen</button>")
                .append("<button type='submit' formaction='/admin/logins/delete/" + benutzer + "'>Löschen</button>")
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
    public String addLogin(@RequestParam String name, @RequestParam String passwort) {
        service.benutzerLogins.put(name, passwort);
        service.bestellListen.put(name, new java.util.ArrayList<>());
        service.statusTexte.put(name, "");
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/delete/{benutzer}")
    public String deleteLogin(@PathVariable String benutzer) {
        service.benutzerLogins.remove(benutzer);
        service.bestellListen.remove(benutzer);
        service.statusTexte.remove(benutzer);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/edit/{benutzer}")
    public String editLogin(@PathVariable String benutzer) {
        return "<script>alert('Bearbeiten aktiv - bitte Textfelder ändern und erneut bestätigen. Funktionalität kann erweitert werden.'); window.location.href='/admin/logins';</script>";
    }

    @GetMapping("/listen")
    public String listenPage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bestelllisten</title><style>")
            .append("body { font-family: Arial; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body><h1>Alle Bestelllisten</h1>");

        for (Map.Entry<String, List<Bestellung>> entry : service.bestellListen.entrySet()) {
            String benutzer = entry.getKey();
            List<Bestellung> liste = entry.getValue();
            html.append("<h2>").append(benutzer).append("</h2>")
                .append("<form action='/admin/listen/update/").append(benutzer).append("' method='post'>")
                .append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th></tr>");
            int index = 0;
            for (Bestellung b : liste) {
                html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>").append(b.getMaterial()).append("</td>")
                    .append("<td><select name='status").append(index).append("'>")
                    .append("<option ").append(b.getStatus().equals("in Bearbeitung") ? "selected" : "").append(">in Bearbeitung</option>")
                    .append("<option ").append(b.getStatus().equals("bestätigt") ? "selected" : "").append(">bestätigt</option>")
                    .append("<option ").append(b.getStatus().equals("Rückgabe fällig") ? "selected" : "").append(">Rückgabe fällig</option>")
                    .append("</select></td></tr>");
                index++;
            }
            html.append("</table><button type='submit'>MatWart OK!</button></form>");
            String status = service.statusTexte.getOrDefault(benutzer, "");
            if (!status.isEmpty()) {
                html.append("<p>").append(status).append("</p>");
            }
        }

        html.append("<br><a href='/admin'>Zurück</a></body></html>");
        return html.toString();
    }

    @PostMapping("/listen/update/{benutzer}")
    public String updateListen(@PathVariable String benutzer, @RequestParam Map<String, String> allParams) {
        List<Bestellung> liste = service.bestellListen.get(benutzer);
        for (int i = 0; i < liste.size(); i++) {
            String statusKey = "status" + i;
            if (allParams.containsKey(statusKey)) {
                liste.get(i).setStatus(allParams.get(statusKey));
            }
        }
        String zeitstempel = new SimpleDateFormat("HH:mm:ss - dd.MM.yyyy").format(new Date());
        service.statusTexte.put(benutzer, zeitstempel + " - von MatWart gesehen");
        return "<script>window.location.href='/admin/listen';</script>";
    }
}
