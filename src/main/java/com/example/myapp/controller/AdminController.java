package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.CsvStorageService;
import com.example.myapp.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AdminController {

    private final LoginService loginService;
    private final CsvStorageService csvService;

    public AdminController(LoginService loginService, CsvStorageService csvService) {
        this.loginService = loginService;
        this.csvService = csvService;
    }

    @GetMapping("/admin")
    @ResponseBody
    public String adminHome() {
        Benutzer u = loginService.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            return "<p>Zugriff verweigert.</p><a href='/'>Zurück</a>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>")
          .append("<style>")
          .append("body { font-family: sans-serif; text-align:center; padding:20px; }")
          .append("a.btn { display:inline-block; padding:10px 20px; margin:10px; border-radius:8px; background:#1976d2; color:white; text-decoration:none; }")
          .append("a.btn:hover { background:#145ca3; }")
          .append("</style></head><body>");

        sb.append("<h2>Adminbereich</h2>");
        sb.append("<p>Angemeldet als <b>").append(u.getUsername()).append("</b></p>");

        sb.append("<a class='btn' href='/admin/benutzer'>Benutzerverwaltung</a>");
        sb.append("<a class='btn' href='/bestellungen'>Bestellungen verwalten</a>");
        sb.append("<a class='btn' href='/materialien'>Materialübersicht öffnen</a>");
        sb.append("<br><br><a href='/logout'>Abmelden</a>");

        sb.append("</body></html>");
        return sb.toString();
    }

    @GetMapping("/admin/benutzer")
    @ResponseBody
    public String showBenutzer() {
        Benutzer u = loginService.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            return "<p>Zugriff verweigert.</p><a href='/'>Zurück</a>";
        }

        List<Benutzer> benutzer = csvService.loadBenutzer();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>")
          .append("<style>")
          .append("body { font-family: sans-serif; text-align:center; padding:20px; }")
          .append("table { border-collapse: collapse; width: 90%; margin:auto; }")
          .append("th, td { border:1px solid #ccc; padding:8px; }")
          .append("th { background:#1976d2; color:white; }")
          .append("</style></head><body>");

        sb.append("<h2>Benutzerverwaltung</h2>");
        sb.append("<table>");
        sb.append("<tr><th>Benutzername</th><th>Passwort</th><th>Admin</th></tr>");

        for (Benutzer b : benutzer) {
            sb.append("<tr>")
              .append("<td>").append(b.getUsername()).append("</td>")
              .append("<td>").append(b.getPasswort()).append("</td>")  // ✅ Passwort sichtbar für Admin
              .append("<td>").append(b.isAdmin() ? "Ja" : "Nein").append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");

        sb.append("<br><a href='/admin'>Zurück</a>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
