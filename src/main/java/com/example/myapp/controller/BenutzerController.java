package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/benutzer")
public class BenutzerController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @GetMapping
    @ResponseBody
    public String list(HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null || !u.isAdmin()) return "Nicht eingeloggt oder keine Berechtigung";

        List<Benutzer> users = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder(htmlHeader("Benutzerverwaltung"));
        sb.append(breadcrumb("Admin", "Benutzerverwaltung"));
        sb.append("<table><tr><th>Benutzername</th><th>Admin</th></tr>");
        for (Benutzer b : users) {
            sb.append("<tr><td>").append(escape(b.getUsername())).append("</td><td>")
              .append(b.isAdmin() ? "Ja" : "Nein").append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<tr>")
          .append("<td data-label='ID'>").append(b.getId()).append("</td>")
          .append("<td data-label='Benutzer'>").append(escape(b.getBenutzer())).append("</td>")
          .append("<td data-label='Material'>").append(escape(b.getMaterial())).append("</td>")
          .append("<td data-label='Anzahl'>").append(b.getAnzahl()).append("</td>")
          .append("<td data-label='Status'>").append(escape(b.getStatus())).append("</td>")
          .append("</tr>");

        // Formular Benutzer hinzufügen
        sb.append("<h3>Neuen Benutzer hinzufügen</h3>");
        sb.append("<form action='/admin/benutzer/add' method='post'>")
          .append("Name: <input name='username' required> ")
          .append("Passwort: <input name='password' required> ")
          .append("Admin: <input type='checkbox' name='admin'> ")
          .append("<button type='submit'>Hinzufügen</button>")
          .append("</form>");

        // Formular Benutzer löschen
        sb.append("<h3>Benutzer löschen</h3>");
        sb.append("<form action='/admin/benutzer/delete' method='post'>")
          .append("Name: <input name='username' required> ")
          .append("<button type='submit'>Löschen</button>")
          .append("</form>");

        sb.append("<nav><a href='/admin'>Zurück zur Übersicht</a></nav>");
        sb.append(htmlFooter());
        return sb.toString();
    }

    @PostMapping("/add")
    @ResponseBody
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(defaultValue = "false") boolean admin) {

        List<String[]> data = csv.readCsv("benutzer.csv");
        data.add(new String[]{username, password, String.valueOf(admin)});
        csv.writeCsv("benutzer.csv", data);

        return "<meta http-equiv='refresh' content='0;url=/admin/benutzer'>";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteUser(@RequestParam String username) {
        List<String[]> data = csv.readCsv("benutzer.csv");
        data.removeIf(r -> r[0].equalsIgnoreCase(username));
        csv.writeCsv("benutzer.csv", data);
        return "<meta http-equiv='refresh' content='0;url=/admin/benutzer'>";
    }
}