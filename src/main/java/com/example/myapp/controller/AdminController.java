package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
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
        .append("    nameField.readOnly = false;")
        .append("    passField.readOnly = false;")
        .append("    btn.textContent = 'Bestätigen';")
        .append("  } else {")
        .append("    if (confirm('Änderungen übernehmen?')) {")
        .append("      var form = document.getElementById('form-' + id);")
        .append("      form.submit();")
        .append("    } else {")
        .append("      nameField.readOnly = true;")
        .append("      passField.readOnly = true;")
        .append("      btn.textContent = 'Anpassen';")
        .append("    }")
        .append("  }")
        .append("}")
        .append("</script></head><body>")
        .append("<h1>Benutzer-Logins</h1>");

    for (String benutzer : service.benutzerLogins.keySet()) {
        String passwort = service.benutzerLogins.get(benutzer);
        html.append("<form id='form-").append(benutzer).append("' action='/admin/logins/update/")
            .append(benutzer).append("' method='post'>")
            .append("<input type='text' id='name-").append(benutzer)
            .append("' name='name' value='").append(benutzer).append("' readonly>")
            .append("<input type='text' id='pass-").append(benutzer)
            .append("' name='passwort' value='").append(passwort).append("' readonly>")
            .append("<button type='button' id='btn-").append(benutzer)
            .append("' onclick='toggleEdit(\"").append(benutzer).append("\")'>Anpassen</button>")
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
        service.benutzerLogins.put(name, passwort);
        service.bestellListen.put(name, new java.util.ArrayList<>());
        service.statusTexte.put(name, "");
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/delete/{benutzer}")
    public String deleteLogin(@PathVariable String benutzer, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        service.benutzerLogins.remove(benutzer);
        service.bestellListen.remove(benutzer);
        service.statusTexte.remove(benutzer);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/edit/{benutzer}")
    public String editLogin(@PathVariable String benutzer, HttpSession session) {
        if (!isAdmin(session)) return redirectToLogin();
        return "<script>alert('Bearbeiten aktiv - bitte Textfelder ändern und erneut bestätigen. Funktionalität kann erweitert werden.'); window.location.href='/admin/logins';</script>";
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

        for (Map.Entry<String, List<Bestellung>> entry : service.bestellListen.entrySet()) {
            String benutzer = entry.getKey();
            
                   // SORTIERUNG:
            List<Bestellung> liste = entry.getValue().stream()
                .sorted(Comparator
                .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
            
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
public String updateListen(@PathVariable String benutzer, @RequestParam Map<String, String> allParams, HttpSession session) {
    if (!isAdmin(session)) return redirectToLogin();
    List<Bestellung> liste = service.getBestellungen(benutzer);
    for (int i = 0; i < liste.size(); i++) {
        String statusKey = "status" + i;
        if (allParams.containsKey(statusKey)) {
            Bestellung b = liste.get(i);
            String neuerStatus = allParams.get(statusKey);
            if (!b.getStatus().equals("Archiviert") && "Archiviert".equals(neuerStatus)) {
                b.setRueckgabedatum(LocalDateTime.now());
            }
            b.setStatus(neuerStatus);
            service.saveBestellung(b);
        }
    }
    service.updateStatusText(benutzer, LocalDateTime.now() + " - von MatWart gesehen");
    return "<script>window.location.href='/admin/listen';</script>";
}
    
    @PostMapping("/logins/update/{oldBenutzer}")
    public String updateLogin(@PathVariable String oldBenutzer, @RequestParam String name, @RequestParam String passwort, HttpSession session) {
    if (!isAdmin(session)) return redirectToLogin();
    
    service.benutzerLogins.remove(oldBenutzer);
    service.benutzerLogins.put(name, passwort);
    
    // optional: auch Bestelllisten und Status umbenennen
    if (!oldBenutzer.equals(name)) {
        service.bestellListen.put(name, service.bestellListen.remove(oldBenutzer));
        service.statusTexte.put(name, service.statusTexte.remove(oldBenutzer));
    }

    return "<script>window.location.href='/admin/logins';</script>";
    }

@GetMapping("/archiv")
public String archivPage(HttpSession session) {
    if (!isAdmin(session)) return redirectToLogin();
    StringBuilder html = new StringBuilder();
    html.append("<html><head><title>Archiv</title><style>")
        .append("body { font-family: Arial; } table { margin: auto; border-collapse: collapse; }")
        .append("td, th { border: 1px solid black; padding: 5px; }")
        .append("</style></head><body><h1>Archivierte Bestellungen</h1>");

    // SORTIERUNG:
    List<Bestellung> alleArchiviert = service.getAlleBestellungen().stream()
        .filter(b -> "Archiviert".equals(b.getStatus()))
        .sorted(Comparator
            .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();

    html.append("<table><tr><th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
    for (Bestellung b : alleArchiviert) {
        html.append("<tr><td>").append(b.getBenutzer()).append("</td><td>")
            .append(b.getAnzahl()).append("</td><td>")
            .append(b.getMaterial()).append("</td><td>")
            .append(b.getEingabedatum() != null ? b.getEingabedatum() : "").append("</td><td>")
            .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum() : "").append("</td></tr>");
    }
    html.append("</table><br><a href='/admin'>Zurück</a></body></html>");
    return html.toString();
}

}

