package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
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

    private String breadcrumb(String path) {
        return "<div class='breadcrumb'><a href='/benutzer'>Home</a> > " + path + "</div>";
    }

    @GetMapping
    public String benutzerHome() {
        return "<html><head><title>Benutzerbereich</title><link rel='stylesheet' href='/style.css'><script src='/script.js'></script></head><body>"
                + "<header><h1>Benutzerbereich</h1></header><main>"
                + "<form method='get' action='/benutzer/material'><button type='submit'>Materialübersicht</button></form>"
                + "<form method='get' action='/benutzer/bestellen'><button type='submit'>Neue Bestellung</button></form>"
                + "<form method='get' action='/benutzer/meineBestellungen'><button type='submit'>Meine Bestellungen</button></form>"
                + "<form method='get' action='/'><button class='btn-back' type='submit'>Logout</button></form>"
                + "</main>" + breadcrumb("Benutzerbereich") + "</body></html>";
    }

    @GetMapping("/material")
    public String materialListe() {
        List<Material> materialien = service.getAlleMaterialien();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Materialübersicht</title><link rel='stylesheet' href='/style.css'><script src='/script.js'></script></head><body>");
        html.append("<header><h1>Materialübersicht</h1></header><main>");
        html.append("<input type='text' class='table-filter' placeholder='Suche Material...' data-table='materialTabelle'>");
        html.append("<table id='materialTabelle'><thead><tr><th>Name</th><th>Bestand</th></tr></thead><tbody>");
        for (Material m : materialien) {
            html.append("<tr><td>").append(m.getName()).append("</td><td>").append(m.getBestand()).append("</td></tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/benutzer'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main>").append(breadcrumb("Materialübersicht")).append("</body></html>");
        return html.toString();
    }

    @GetMapping("/bestellen")
    public String bestellForm() {
        List<Material> materialien = service.getAlleMaterialien();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Neue Bestellung</title><link rel='stylesheet' href='/style.css'></head><body>");
        html.append("<header><h1>Neue Bestellung</h1></header><main>");
        html.append("<form method='post' action='/benutzer/bestellen'>");
        html.append("Material: <select name='material'>");
        for (Material m : materialien) {
            html.append("<option value='").append(m.getName()).append("'>").append(m.getName()).append("</option>");
        }
        html.append("</select><br>");
        html.append("Anzahl: <input type='number' name='anzahl' min='1' required><br>");
        html.append("<button type='submit'>Bestellen</button>");
        html.append("</form>");
        html.append("<form method='get' action='/benutzer'><button class='btn-back' type='submit'>Abbrechen</button></form>");
        html.append("</main>").append(breadcrumb("Neue Bestellung")).append("</body></html>");
        return html.toString();
    }

    @PostMapping("/bestellen")
    public String neueBestellung(@RequestParam String material, @RequestParam int anzahl) {
        service.addBestellung(material, anzahl);
        return "<script>window.location.href='/benutzer/meineBestellungen';</script>";
    }

    @GetMapping("/meineBestellungen")
    public String meineBestellungen() {
        List<Bestellung> bestellungen = service.getMeineBestellungen();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Meine Bestellungen</title><link rel='stylesheet' href='/style.css'><script src='/script.js'></script></head><body>");
        html.append("<header><h1>Meine Bestellungen</h1></header><main>");
        html.append("<input type='text' class='table-filter' placeholder='Suche Bestellungen...' data-table='meineBestellungenTabelle'>");
        html.append("<table id='meineBestellungenTabelle'><thead><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th></tr></thead><tbody>");
        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td>").append(b.getStatus()).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/benutzer'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append("</main>").append(breadcrumb("Meine Bestellungen")).append("</body></html>");
        return html.toString();
    }
}
