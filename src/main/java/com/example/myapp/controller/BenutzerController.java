package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/normalbenutzer")
public class BenutzerController {

    private final ListeService service;

    public BenutzerController(ListeService service) {
        this.service = service;
    }

    private String htmlHead(String title) {
        return "<html><head><meta charset='UTF-8'><title>" + title + "</title>"
             + "<link rel='stylesheet' href='/style.css'>"
             + "</head><body>"
             + "<header>"
             + "<img src='/images/Logo_Pfadi_Panthera_Leo.png' alt='Logo' style='height:80px;'>"
             + "<h1>" + title + "</h1>"
             + "</header>";
    }

    @GetMapping("/{benutzer}")
    public String benutzerSeite(@PathVariable String benutzer) {
        List<Bestellung> bestellungen = service.getBestellungen(benutzer);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append(htmlHead("Willkommen, " + benutzer));
        html.append("<form action='/normalbenutzer/").append(benutzer).append("/bestellen' method='post'>")
            .append("Anzahl: <input name='anzahl' type='number' required> ")
            .append("Material: <input name='material' required> ")
            .append("<button type='submit'>Bestellen</button>")
            .append("</form>");

        html.append("<h2>Deine Bestellungen:</h2>");
        html.append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Rückgabedatum</th><th>Löschen</th></tr>");

        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(b.getMaterial()).append("</td>")
                .append("<td>").append(b.getStatus()).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>");
            if ("in Bearbeitung".equals(b.getStatus()) && b.getEingabedatum() == null) {
                html.append("<td><form action='/normalbenutzer/").append(benutzer).append("/loeschen/")
                    .append(b.getId()).append("' method='post'><button type='submit'>X</button></form></td>");
            } else {
                html.append("<td>-</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");

        html.append("<form action='/normalbenutzer/").append(benutzer).append("/senden' method='post'>")
            .append("<button type='submit'>An MatWart senden</button></form>");

        html.append("<br><a href='/'>Logout</a>");
        html.append("</body></html>");
        return html.toString();
    }

    @PostMapping("/{benutzer}/bestellen")
    public String bestelle(@PathVariable String benutzer, @RequestParam int anzahl, @RequestParam String material) {
        service.bestelle(benutzer, anzahl, material);
        return "<meta http-equiv='refresh' content='0; URL=/normalbenutzer/" + benutzer + "'>";
    }

    @PostMapping("/{benutzer}/senden")
    public String senden(@PathVariable String benutzer) {
        service.markiereAlsAbgegeben(benutzer);
        return "<meta http-equiv='refresh' content='0; URL=/normalbenutzer/" + benutzer + "'>";
    }

    @PostMapping("/{benutzer}/loeschen/{id}")
    public String loeschen(@PathVariable String benutzer, @PathVariable Long id) {
        service.loescheBestellungWennMoeglich(id);
        return "<meta http-equiv='refresh' content='0; URL=/normalbenutzer/" + benutzer + "'>";
    }
}
