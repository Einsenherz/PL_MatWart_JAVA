package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/{benutzer}")
    public String benutzerSeite(@PathVariable String benutzer, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedInUser)) return "<script>window.location.href='/'</script>";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(service.getZone());
        List<Bestellung> bestellungen = service.getBestellungen(benutzer).stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .toList();

        StringBuilder html = new StringBuilder("<h1>Willkommen ").append(benutzer).append("</h1>");
        html.append("<form action='/normalbenutzer/").append(benutzer).append("/bestellen' method='post'>")
            .append("<input type='number' name='anzahl' min='1'><input type='text' name='material'>")
            .append("<button type='submit'>Bestellen</button></form>");
        html.append("<table><tr><th>Anzahl</th><th>Material</th><th>Status</th><th>Eingabedatum</th><th>Aktion</th></tr>");
        for (Bestellung b : bestellungen) {
            html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>").append(b.getMaterial()).append("</td><td>")
                    .append(b.getStatus()).append("</td><td>")
                    .append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td><td>");
            if ("in Bearbeitung".equals(b.getStatus()) && b.getEingabedatum() == null) {
                html.append("<form method='post' action='/normalbenutzer/").append(benutzer).append("/delete/").append(b.getId()).append("'>")
                    .append("<button type='submit'>🗑️</button></form>");
            }
            html.append("</td></tr>");
        }
        html.append("</table>");
        html.append("<form action='/normalbenutzer/").append(benutzer).append("/senden' method='post'>")
            .append("<button type='submit'>An MatWart senden</button></form>");
        return html.toString();
    }

    @PostMapping("/{benutzer}/bestellen")
    public String bestellen(@PathVariable String benutzer, @RequestParam int anzahl, @RequestParam String material, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedInUser)) return "<script>window.location.href='/'</script>";
        service.bestelle(benutzer, anzahl, material);
        return "<script>window.location.href='/normalbenutzer/" + benutzer + "'</script>";
    }

    @PostMapping("/{benutzer}/senden")
    public String senden(@PathVariable String benutzer, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedInUser)) return "<script>window.location.href='/'</script>";
        service.markiereAlsAbgegeben(benutzer);
        return "<script>window.location.href='/normalbenutzer/" + benutzer + "'</script>";
    }

    @PostMapping("/{benutzer}/delete/{id}")
    public String delete(@PathVariable String benutzer, @PathVariable Long id, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedInUser)) return "<script>window.location.href='/'</script>";
        Bestellung b = service.getBestellungen(benutzer).stream().filter(best -> best.getId().equals(id)).findFirst().orElse(null);
        if (b != null && "in Bearbeitung".equals(b.getStatus()) && b.getEingabedatum() == null) {
            service.deleteBestellung(id);
        }
        return "<script>window.location.href='/normalbenutzer/" + benutzer + "'</script>";
    }
}
