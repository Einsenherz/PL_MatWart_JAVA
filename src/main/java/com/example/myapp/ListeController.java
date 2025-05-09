package com.example.myapp;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ListeController {
    private final List<String> liste = new ArrayList<>(List.of("Eintrag 1", "Eintrag 2", "Eintrag 3"));
    private final List<String> bestellListe = new ArrayList<>();

    private final String ADMIN_PASS = "adminpass"; // Passwort für Admin
    private final String USER_PASS = "userpass"; // Passwort für Normalbenutzer

    @GetMapping("/")
    public String home() {
        return """
                <html><head><title>Login</title><style>
                body { text-align: center; font-family: Arial; margin-top: 100px; }
                input, button { font-size: 16px; }
                </style></head><body>
                <h1>Panthera Leo MatWart Login</h1>
                <form action="/login" method="post">
                    <input type="password" name="password" placeholder="Passwort eingeben" required>
                    <button type="submit">Bestätigen</button>
                </form>
                </body></html>
                """;
    }

    @PostMapping("/login")
    public String login(@RequestParam String password) {
        if (password.equals(ADMIN_PASS)) {
            return "<script>window.location.href='/admin';</script>";
        } else if (password.equals(USER_PASS)) {
            return "<script>window.location.href='/normalbenutzer';</script>";
        } else {
            return "<p>Falsches Passwort! <a href='/'>Zurück</a></p>";
        }
    }

    @GetMapping("/admin")
    public String adminPage() {
        StringBuilder html = new StringBuilder();
        html.append("""
                <html><head><title>Admin</title><style>
                body { text-align: center; font-family: Arial; margin-top: 50px; }
                button { margin: 5px; font-size: 16px; }
                </style></head><body>
                <h1>Admin-Bereich</h1>
                <button onclick="window.location.href='/addEintrag'">Hinzufügen</button>
                <button onclick="window.location.href='/removeEintrag'">Löschen</button>
                <h2>Liste:</h2>
                """);
        for (String eintrag : liste) {
            html.append(eintrag).append("<br>");
        }
        html.append("<br><a href='/'>Logout</a></body></html>");
        return html.toString();
    }

    @GetMapping("/addEintrag")
    public String addEintrag() {
        liste.add("Neuer Eintrag " + (liste.size() + 1));
        return "<script>window.location.href='/admin';</script>";
    }

    @GetMapping("/removeEintrag")
    public String removeEintrag() {
        if (!liste.isEmpty()) {
            liste.remove(liste.size() - 1);
        }
        return "<script>window.location.href='/admin';</script>";
    }

    @GetMapping("/normalbenutzer")
    public String normalBenutzerPage() {
        StringBuilder html = new StringBuilder();
        html.append("""
                <html><head><title>Normalbenutzer</title><style>
                body { text-align: center; font-family: Arial; margin-top: 50px; }
                input, select, button { font-size: 16px; margin: 5px; }
                </style></head><body>
                <h1>Normalbenutzer-Bereich</h1>
                <form action="/bestellen" method="post">
                    <input type="number" name="anzahl" min="1" placeholder="Anzahl" required>
                    <select name="eintrag">
                """);

        for (String eintrag : liste) {
            html.append("<option value=\"").append(eintrag).append("\">").append(eintrag).append("</option>");
        }

        html.append("""
                    </select>
                    <button type="submit">Bestätigen</button>
                </form>
                <h2>Bestellliste:</h2>
                """);

        for (int i = 0; i < bestellListe.size(); i++) {
            html.append(bestellListe.get(i))
                .append(" <a href=\"/deleteBestellung/")
                .append(i)
                .append("\" onclick=\"return confirm('Wirklich löschen?');\">🗑️</a><br>");
        }

        html.append("<br><a href='/'>Logout</a></body></html>");
        return html.toString();
    }

    @PostMapping("/bestellen")
    public String bestellen(@RequestParam int anzahl, @RequestParam String eintrag) {
        bestellListe.add(anzahl + "x " + eintrag);
        return "<script>window.location.href='/normalbenutzer';</script>";
    }

    @GetMapping("/deleteBestellung/{index}")
    public String deleteBestellung(@PathVariable int index) {
        if (index >= 0 && index < bestellListe.size()) {
            bestellListe.remove(index);
        }
        return "<script>window.location.href='/normalbenutzer';</script>";
    }
}
