package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
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

    private String breadcrumb(String path) {
        return "<div class='breadcrumb'><a href='/benutzer'>Home</a> > " + path + "</div>";
    }

    @GetMapping
    public String benutzerHome(HttpSession session) {
        String benutzer = (String) session.getAttribute("username");
        if (benutzer == null) {
            return "<script>alert('Bitte zuerst einloggen!');window.location.href='/';</script>";
        }

        return "<html><head><title>Benutzerbereich</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "<script src='/script.js'></script>"
                + "</head><body>"
                + "<header><h1>Willkommen, " + benutzer + "</h1></header>"
                + "<main class='centered-content'>"
                + "<form method='get' action='/benutzer/bestellen'><button type='submit'>Material bestellen</button></form>"
                + "<form method='get' action='/benutzer/meine-bestellungen'><button type='submit'>Meine Bestellungen</button></form>"
                + "<form method='get' action='/logout'><button class='btn-back' type='submit'>Logout</button></form>"
                + "</main>"
                + breadcrumb("Benutzerbereich")
                + "</body></html>";
    }

    @GetMapping("/bestellen")
    public String bestellenForm(HttpSession session) {
        String benutzer = (String) session.getAttribute("username");
        if (benutzer == null) {
            return "<script>alert('Bitte zuerst einloggen!');window.location.href='/';</script>";
        }

        List<Material> materialien = service.getAlleMaterialien();
        StringBuilder html = new StringBuilder("<html><head><title>Material bestellen</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "</head><body>");
        html.append("<header><h1>Material bestellen</h1></header><main class='centered-content'>");
        html.append("<form class='styled-form' method='post' action='/benutzer/bestellen'>");
        html.append("<label>Material:</label> <select name='material'>");
        for (Material m : materialien) {
            html.append("<option value='").append(m.getName()).append("'>")
                .append(m.getName()).append(" (Bestand: ").append(m.getBestand()).append(")")
                .append("</option>");
        }
        html.append("</select>");
        html.append("<label>Anzahl:</label> <input type='number' name='anzahl' min='1' required>");
        html.append("<br><button type='submit'>Bestellen</button></form>");
        html.append("<form method='get' action='/benutzer'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main>").append(breadcrumb("Material bestellen")).append("</body></html>");
        return html.toString();
    }

    @PostMapping("/bestellen")
    public String bestellen(@RequestParam String material, @RequestParam int anzahl, HttpSession session) {
        String benutzer = (String) session.getAttribute("username");
        if (benutzer == null) {
            return "<script>alert('Bitte zuerst einloggen!');window.location.href='/';</script>";
        }
        service.addBestellung(benutzer, material, anzahl);
        return "<script>window.location.href='/benutzer/meine-bestellungen';</script>";
    }

    @GetMapping("/meine-bestellungen")
    public String meineBestellungen(HttpSession session) {
        String benutzer = (String) session.getAttribute("username");
        if (benutzer == null) {
            return "<script>alert('Bitte zuerst einloggen!');window.location.href='/';</script>";
        }

        List<Bestellung> bestellungen = service.getMeineBestellungen(benutzer);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder("<html><head><title>Meine Bestellungen</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "<script src='/script.js'></script></head><body>");
        html.append("<header><h1>Meine Bestellungen</h1></header><main>");
        html.append("<input type='text' class='table-filter' placeholder='Suche Bestellungen...' data-table='meineBestellungenTabelle'>");
        html.append("<table id='meineBestellungenTabelle'><thead><tr>"
                + "<th>Material</th><th>Anzahl</th><th>Status</th><th>Eingabedatum</th></tr></thead><tbody>");
        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getStatus()).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/benutzer'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main>").append(breadcrumb("Meine Bestellungen")).append("</body></html>");
        return html.toString();
    }
}
