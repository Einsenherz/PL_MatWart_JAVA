package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;

import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/benutzer")
public class BenutzerController {
    private final ListeService service;

    public BenutzerController(ListeService service) {
        this.service = service;
    }

    @GetMapping
    public String benutzerHome(@RequestParam String username) {
        return "<html><head><title>Benutzerbereich</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "</head><body>"
                + "<header><h1>Willkommen, " + username + "</h1></header>"
                + "<main>"
                + "<form method='get' action='/benutzer/listen'><input type='hidden' name='username' value='" + username + "'><button type='submit'>Meine Bestellungen</button></form>"
                + "<form method='get' action='/benutzer/bestellen'><input type='hidden' name='username' value='" + username + "'><button type='submit'>Neue Bestellung</button></form>"
                + "<form method='get' action='/'><button class='btn-back' type='submit'>Logout</button></form>"
                + "</main></body></html>";
    }

    @GetMapping("/listen")
    public String meineBestellungen(@RequestParam String username) {
        List<Bestellung> bestellungen = service.getBestellungenVonBenutzer(username);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Meine Bestellungen</title><link rel='stylesheet' href='/style.css'></head><body>");
        html.append("<header><h1>Meine Bestellungen</h1></header><main>");
        html.append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr>");
        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td>").append(b.getStatus()).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("</tr>");
        }
        html.append("</table>");
        html.append("<form method='get' action='/benutzer'><input type='hidden' name='username' value='" + username + "'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main></body></html>");
        return html.toString();
    }

    @GetMapping("/bestellen")
    public String bestellFormular(@RequestParam String username) {
        return "<html><head><title>Neue Bestellung</title><link rel='stylesheet' href='/style.css'></head><body>"
                + "<header><h1>Neue Bestellung</h1></header><main>"
                + "<form method='post' action='/benutzer/bestellen'>"
                + "<input type='hidden' name='username' value='" + username + "'>"
                + "Anzahl: <input type='number' name='anzahl' required><br>"
                + "Material: <input type='text' name='material' required><br>"
                + "<button type='submit'>Bestellen</button></form>"
                + "<form method='get' action='/benutzer'><input type='hidden' name='username' value='" + username + "'><button class='btn-back' type='submit'>Zurück</button></form>"
                + "</main></body></html>";
    }

    @PostMapping("/bestellen")
    public String bestellungAbsenden(@RequestParam String username, @RequestParam int anzahl, @RequestParam String material) {
        service.addBestellung(username, anzahl, material);
        return "<script>window.location.href='/benutzer/listen?username=" + username + "';</script>";
    }
}
