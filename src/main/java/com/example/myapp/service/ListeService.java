package com.example.myapp.service;

import com.example.myapp.model.Bestellung;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ListeService {
    public final Map<String, String> benutzerLogins = new HashMap<>();
    public final Map<String, List<Bestellung>> bestellListen = new HashMap<>();
    public final Map<String, String> statusTexte = new HashMap<>();

    public ListeService() {
        benutzerLogins.put("max", "1234");
        benutzerLogins.put("anna", "5678");
        bestellListen.put("max", new ArrayList<>());
        bestellListen.put("anna", new ArrayList<>());
        statusTexte.put("max", "");
        statusTexte.put("anna", "");
    }

    public String generiereBenutzerSeite(String benutzer) {
        List<Bestellung> bestellungen = bestellListen.getOrDefault(benutzer, new ArrayList<>());
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Benutzer</title><style>")
            .append("body { text-align: center; font-family: Arial; margin-top: 50px; }")
            .append("input, button { font-size: 16px; margin: 5px; }")
            .append("table { margin: auto; border-collapse: collapse; }")
            .append("td, th { border: 1px solid black; padding: 5px; }")
            .append("</style></head><body>")
            .append("<h1>Willkommen, ").append(benutzer).append("!</h1>")
            .append("<form action='/normalbenutzer/").append(benutzer).append("/bestellen' method='post'>")
            .append("<input type='number' name='anzahl' min='1' placeholder='Anzahl' required>")
            .append("<input type='text' name='material' placeholder='Material' required>")
            .append("<button type='submit'>Bestätigen</button></form>")
            .append("<h2>Bestellliste:</h2><table><tr><th>Anzahl</th><th>Material</th></tr>");
        for (Bestellung b : bestellungen) {
            html.append("<tr><td>").append(b.getAnzahl()).append("</td><td>").append(b.getMaterial()).append("</td></tr>");
        }
        html.append("</table><br><form action='/normalbenutzer/").append(benutzer).append("/senden' method='post'>")
            .append("<button type='submit'>An MatWart senden!</button></form>");
        String status = statusTexte.getOrDefault(benutzer, "");
        if (!status.isEmpty()) {
            html.append("<p>").append(status).append("</p>");
        }
        html.append("<br><a href='/'>Logout</a></body></html>");
        return html.toString();
    }

    public void bestelle(String benutzer, int anzahl, String material) {
        bestellListen.get(benutzer).add(new Bestellung(anzahl, material, "in Bearbeitung"));
    }

    public void markiereAlsAbgegeben(String benutzer) {
        String zeitstempel = new SimpleDateFormat("HH:mm:ss - dd.MM.yyyy").format(new Date());
        statusTexte.put(benutzer, zeitstempel + " - an MatWart abgegeben");
    }
}
