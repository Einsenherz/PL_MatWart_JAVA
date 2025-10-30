package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Benutzer;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/bestellungen", "/admin/bestellungen"})
public class BestellungController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @GetMapping
    @ResponseBody
    public String list(HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null) return "Nicht eingeloggt";

        List<Bestellung> best = csv.readCsv("bestellungen.csv").stream()
                .map(Bestellung::fromCsv).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder(htmlHeader("Bestellungen"));
        sb.append(breadcrumb(u.isAdmin() ? "Admin" : "Home", "Bestellungen"));

        sb.append("<div class='table-container'><table>")
          .append("<tr><th>ID</th><th>Benutzer</th><th>Material</th><th>Anzahl</th><th>Status</th></tr>");

        for (Bestellung b : best) {
            if (u.isAdmin() || b.getBenutzer().equalsIgnoreCase(u.getUsername())) {
                sb.append("<tr>")
                  .append("<td data-label='ID'>").append(b.getId()).append("</td>")
                  .append("<td data-label='Benutzer'>").append(escape(b.getBenutzer())).append("</td>")
                  .append("<td data-label='Material'>").append(escape(b.getMaterial())).append("</td>")
                  .append("<td data-label='Anzahl'>").append(b.getAnzahl()).append("</td>")
                  .append("<td data-label='Status'>").append(escape(b.getStatus())).append("</td>")
                  .append("</tr>");
            }
        }

        sb.append("</table></div>");

        // Formular: Bestellung hinzufügen (nur User)
        if (!u.isAdmin()) {
            sb.append("<h3>Neue Bestellung</h3>");
            sb.append("<form action='/bestellungen/add' method='post'>")
              .append("Material: <input name='material' required> ")
              .append("Anzahl: <input type='number' name='anzahl' min='1' required> ")
              .append("<button type='submit'>Bestellen</button>")
              .append("</form>");
        }

        // Formular: Bestellung löschen (User/Admin)
        sb.append("<h3>Bestellung löschen</h3>");
        sb.append("<form action='/bestellungen/delete' method='post'>")
          .append("ID: <input type='number' name='id' required> ")
          .append("<button type='submit'>Löschen</button>")
          .append("</form>");

        // Formular: Status ändern (nur Admin)
        if (u.isAdmin()) {
            sb.append("<h3>Bestellstatus ändern</h3>");
            sb.append("<form action='/bestellungen/status' method='post'>")
              .append("ID: <input type='number' name='id' required> ")
              .append("Neuer Status: <input name='status' required> ")
              .append("<button type='submit'>Aktualisieren</button>")
              .append("</form>");
        }

        sb.append("<nav><a href='").append(u.isAdmin() ? "/admin" : "/home").append("'>Zurück</a></nav>");
        sb.append(htmlFooter());
        return sb.toString();
    }

    @PostMapping("/add")
    @ResponseBody
    public String addBestellung(HttpSession session,
                                @RequestParam String material,
                                @RequestParam int anzahl) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null) return "Nicht eingeloggt";

        List<String[]> rows = csv.readCsv("bestellungen.csv");
        int newId = rows.size() + 1;
        rows.add(new String[]{
                String.valueOf(newId),
                u.getUsername(),
                material,
                String.valueOf(anzahl),
                "offen"
        });
        csv.writeCsv("bestellungen.csv", rows);
        return "<meta http-equiv='refresh' content='0;url=/bestellungen'>";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteBestellung(@RequestParam int id, HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null) return "Nicht eingeloggt";

        List<String[]> rows = csv.readCsv("bestellungen.csv");
        rows.removeIf(r -> {
            boolean match = Integer.parseInt(r[0]) == id;
            boolean allowed = u.isAdmin() || r[1].equalsIgnoreCase(u.getUsername());
            return match && allowed;
        });
        csv.writeCsv("bestellungen.csv", rows);
        return "<meta http-equiv='refresh' content='0;url=/bestellungen'>";
    }

    @PostMapping("/status")
    @ResponseBody
    public String updateStatus(@RequestParam int id,
                               @RequestParam String status,
                               HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null || !u.isAdmin()) return "Keine Berechtigung";

        List<String[]> rows = csv.readCsv("bestellungen.csv");
        for (String[] r : rows) {
            if (Integer.parseInt(r[0]) == id) {
                r[4] = status;
                break;
            }
        }
        csv.writeCsv("bestellungen.csv", rows);
        return "<meta http-equiv='refresh' content='0;url=/admin/bestellungen'>";
    }
}