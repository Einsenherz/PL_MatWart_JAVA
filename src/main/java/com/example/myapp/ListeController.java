package com.example.myapp;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ListeController {
    private List<String> liste = new ArrayList<>(List.of("Eintrag 1", "Eintrag 2", "Eintrag 3"));

    @GetMapping("/")
    public String home() {
        StringBuilder html = new StringBuilder();
        html.append("<h1>Willkommen auf der Website zur Materialverwaltung</h1>");
        html.append("<h2>Aktuelle Liste:</h2>");
        for (int i = 0; i < liste.size(); i++) {
            html.append(liste.get(i))
                .append(" <a href=\"/delete/")
                .append(i)
                .append("\">[Löschen]</a><br>");
        }
        html.append("<br>");
        html.append("<a href=\"/add?wert=Neuer%20Eintrag\">[Neuen Eintrag hinzufügen]</a>");
        return html.toString();
    }

    @GetMapping("/add")
    public String addEintrag(@RequestParam String wert) {
        liste.add(wert);
        return "<p>Eintrag hinzugefügt.</p><a href=\"/\">Zurück zur Startseite</a>";
    }

    @GetMapping("/delete/{index}")
    public String deleteEintrag(@PathVariable int index) {
        if (index >= 0 && index < liste.size()) {
            liste.remove(index);
        }
        return "<p>Eintrag gelöscht.</p><a href=\"/\">Zurück zur Startseite</a>";
    }
}

