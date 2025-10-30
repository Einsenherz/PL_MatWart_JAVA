package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/benutzer")
public class BenutzerController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @GetMapping
    @ResponseBody
    public String list(
            HttpSession session,
            @RequestParam(value = "q", required = false) String query
    ) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null || !u.isAdmin()) return "Nicht eingeloggt oder keine Berechtigung";

        // Alle Benutzer laden & sortieren
        List<Benutzer> allUsers = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv)
                .sorted(Comparator.comparing(b -> b.getUsername().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        // Optional filtern (Suche)
        List<Benutzer> users = allUsers;
        String effectiveQuery = query == null ? "" : query.trim();
        if (!effectiveQuery.isEmpty()) {
            String qLower = effectiveQuery.toLowerCase(Locale.ROOT);
            users = allUsers.stream()
                    .filter(b -> b.getUsername() != null && b.getUsername().toLowerCase(Locale.ROOT).contains(qLower))
                    .collect(Collectors.toList());
        }

        StringBuilder sb = new StringBuilder(htmlHeader("Benutzerverwaltung"));
        sb.append(breadcrumb("Admin", "Benutzerverwaltung"));

        // Suchleiste mit datalist (Vorschläge)
        sb.append("<h3>Benutzer suchen</h3>");
        sb.append("<form action='/admin/benutzer' method='get' class='form-inline'>")
          .append("<input type='text' name='q' list='userOptions' placeholder='Benutzername suchen' value='").append(escape(effectiveQuery)).append("' /> ")
          .append("<datalist id='userOptions'>");
        for (Benutzer b : allUsers) {
            sb.append("<option value='").append(escape(b.getUsername())).append("'></option>");
        }
        sb.append("</datalist>")
          .append("<button type='submit'>Suchen</button> ")
          .append("<a href='/admin/benutzer' class='btn-link'>Zurücksetzen</a>")
          .append("</form>");

        // Tabelle
        sb.append("<div class='table-container'><table>")
          .append("<tr><th>Benutzername</th><th>Admin</th></tr>");

        for (Benutzer b : users) {
            sb.append("<tr>")
              .append("<td data-label='Benutzername'>").append(escape(b.getUsername())).append("</td>")
              .append("<td data-label='Admin'>").append(b.isAdmin() ? "Ja" : "Nein").append("</td>")
              .append("</tr>");
        }

        sb.append("</table></div>");

        // Formular: Benutzer hinzufügen
        sb.append("<h3>Neuen Benutzer hinzufügen</h3>");
        sb.append("<form action='/admin/benutzer/add' method='post'>")
          .append("<label>Name:")
          .append("<input name='username' required>")
          .append("</label>")
          .append("<label>Passwort:")
          .append("<input name='password' required>")
          .append("</label>")
          .append("<label style='display:flex;align-items:center;gap:8px;'>")
          .append("<input type='checkbox' name='admin'> Admin")
          .append("</label>")
          .append("<button type='submit'>Hinzufügen</button>")
          .append("</form>");

        // Formular: Benutzer löschen (Dropdown + Bestätigung)
        sb.append("<h3>Benutzer löschen</h3>");
        sb.append("<form action='/admin/benutzer/delete' method='post' onsubmit=\"return confirm('Willst du diesen Benutzer wirklich löschen? Dieser Vorgang kann nicht rückgängig gemacht werden.');\">")
          .append("<label>Benutzer auswählen:")
          .append("<select name='username' required>");
        for (Benutzer b : allUsers) {
            sb.append("<option value='").append(escape(b.getUsername())).append("'>")
              .append(escape(b.getUsername()))
              .append(b.isAdmin() ? " (Admin)" : "")
              .append("</option>");
        }
        sb.append("</select>")
          .append("</label>")
          .append("<button type='submit' style='background:#c12525;'>Löschen</button>")
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