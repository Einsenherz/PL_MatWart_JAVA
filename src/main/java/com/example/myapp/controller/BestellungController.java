package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Material;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/bestellungen", "/admin/bestellungen"})
public class BestellungController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @ResponseBody
    public String list(HttpSession session,
                       @RequestParam(value = "user", required = false) String filterUser) {

        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null) return "Nicht eingeloggt";

        // CSV-Daten laden
        List<Bestellung> best = csv.readCsv("bestellungen.csv").stream()
                .map(Bestellung::fromCsv).collect(Collectors.toList());
        List<Benutzer> allUsers = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv).collect(Collectors.toList());
        List<Material> mats = csv.readCsv("material.csv").stream()
                .map(Material::fromCsv).collect(Collectors.toList());

        // Filter für Admin (nach Benutzer)
        if (filterUser != null && !filterUser.isEmpty() && u.isAdmin()) {
            best = best.stream()
                    .filter(b -> b.getBenutzer().equalsIgnoreCase(filterUser))
                    .collect(Collectors.toList());
        } else if (!u.isAdmin()) {
            best = best.stream()
                    .filter(b -> b.getBenutzer().equalsIgnoreCase(u.getUsername()))
                    .collect(Collectors.toList());
        }

        // HTML Header
        StringBuilder sb = new StringBuilder(htmlHeader("Bestellungen"));
        sb.append(breadcrumb(u.isAdmin() ? "Admin" : "Home", "Bestellungen"));

        // Benutzerfilter (Admin)
        if (u.isAdmin()) {
            sb.append("<form action='/admin/bestellungen' method='get' class='form-inline'>")
              .append("<label>Benutzer: ")
              .append("<input type='text' name='user' list='userlist' placeholder='Benutzername' value='")
              .append(filterUser == null ? "" : escape(filterUser)).append("' />")
              .append("<datalist id='userlist'>");
            for (Benutzer bu : allUsers)
                sb.append("<option value='").append(escape(bu.getUsername())).append("'></option>");
            sb.append("</datalist>")
              .append("<button type='submit'>Filtern</button>")
              .append("<a href='/admin/bestellungen' class='btn-link'>Alle</a>")
              .append("</label></form>");
        }

        // Tabelle
        sb.append("<div class='table-container'><table>")
          .append("<tr><th>ID</th><th>Benutzer</th><th>Material</th><th>Anzahl</th><th>Status</th><th>Eingabezeit</th><th>Updated</th></tr>");

        for (Bestellung b : best) {
            String status = b.getStatus();
            String colorClass =
                    status.equalsIgnoreCase("Erledigt") ? "status-done" :
                    status.equalsIgnoreCase("Abgeschlossen") ? "status-finished" :
                    "status-open";

            sb.append("<tr class='").append(colorClass).append("'>")
              .append("<td data-label='ID'>").append(b.getId()).append("</td>")
              .append("<td data-label='Benutzer'>").append(escape(b.getBenutzer())).append("</td>")
              .append("<td data-label='Material'>").append(escape(b.getMaterial())).append("</td>")
              .append("<td data-label='Anzahl'>").append(b.getAnzahl()).append("</td>")
              .append("<td data-label='Status'>").append("<span class='status-tag ").append(colorClass).append("'>")
              .append(escape(status)).append("</span></td>")
              .append("<td data-label='Eingabezeit'>").append(escape(b.getEingabeZeit())).append("</td>")
              .append("<td data-label='Updated'>").append(escape(b.getUpdated())).append("</td>")
              .append("</tr>");
        }

        sb.append("</table></div>");

        // Formular: neue Bestellung (nur User)
        if (!u.isAdmin()) {
            sb.append("<h3>Neue Bestellung</h3>")
              .append("<form action='/bestellungen/add' method='post'>")
              .append("<div id='multiItems'>").append(renderMaterialSelect(mats, 1)).append("</div>")
              .append("<button type='button' onclick='addItem()'>+ weiteres Material</button><br><br>")
              .append("<button type='submit'>Bestellen</button>")
              .append("</form>")
              .append("<script>")
              .append("function addItem(){const d=document.getElementById('multiItems');d.insertAdjacentHTML('beforeend',`")
              .append(renderMaterialSelect(mats,-1).replace("`","\\`")).append("`);}")
              .append("</script>");
        }

        // Formular: löschen
        sb.append("<h3>Bestellung löschen</h3>")
          .append("<form action='/bestellungen/delete' method='post'>")
          .append("ID: <input type='number' name='id' required>")
          .append("<button type='submit' style='background:#b22'>Löschen</button>")
          .append("</form>");

        // Formular: Status ändern (Admin)
        if (u.isAdmin()) {
            sb.append("<h3>Bestellstatus ändern</h3>")
              .append("<form action='/bestellungen/status' method='post'>")
              .append("ID: <input type='number' name='id' required> ")
              .append("Status: <select name='status'>")
              .append("<option value='Offen'>Offen</option>")
              .append("<option value='Erledigt'>Erledigt</option>")
              .append("<option value='Abgeschlossen'>Abgeschlossen</option>")
              .append("</select>")
              .append("<button type='submit'>Aktualisieren</button>")
              .append("</form>");
        }

        sb.append("<nav><a href='").append(u.isAdmin() ? "/admin" : "/home").append("'>Zurück</a></nav>")
          .append(htmlFooter());
        return sb.toString();
    }

    // Materialauswahl
    private String renderMaterialSelect(List<Material> mats, int index) {
        StringBuilder s = new StringBuilder("<div class='material-line'>");
        s.append("Material: <input list='mats' name='material' required placeholder='Material suchen...'> ")
         .append("<datalist id='mats'>");
        for (Material m : mats) {
            s.append("<option value='").append(escape(m.getName())).append("' data-max='")
             .append(m.getBestand()).append("'></option>");
        }
        s.append("</datalist>")
         .append("Anzahl: <input type='number' name='anzahl' min='1' required>")
         .append("</div>");
        return s.toString();
    }

    // POST: Neue Bestellung
    @PostMapping("/add")
    @ResponseBody
    public String addBestellung(HttpSession session,
                                @RequestParam List<String> material,
                                @RequestParam List<Integer> anzahl) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null) return "Nicht eingeloggt";

        List<String[]> rows = csv.readCsv("bestellungen.csv");
        int id = rows.size() + 1;
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < material.size(); i++) {
            rows.add(new String[]{
                    String.valueOf(id++),
                    u.getUsername(),
                    material.get(i),
                    String.valueOf(anzahl.get(i)),
                    "Offen",
                    fmt.format(now),
                    fmt.format(now)
            });
        }
        csv.writeCsv("bestellungen.csv", rows);
        return "<meta http-equiv='refresh' content='0;url=/bestellungen'>";
    }

    // POST: Löschen
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

    // POST: Status ändern + Lagerlogik + Zeitupdate
    @PostMapping("/status")
    @ResponseBody
    public String updateStatus(@RequestParam int id,
                               @RequestParam String status,
                               HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null || !u.isAdmin()) return "Keine Berechtigung";

        List<String[]> rows = csv.readCsv("bestellungen.csv");
        List<String[]> mats = csv.readCsv("material.csv");

        for (String[] r : rows) {
            if (Integer.parseInt(r[0]) == id) {
                String oldStatus = r[4];
                r[4] = status;
                r[6] = fmt.format(LocalDateTime.now());
                String matName = r[2];
                int qty = Integer.parseInt(r[3]);

                for (String[] m : mats) {
                    if (m[0].equalsIgnoreCase(matName)) {
                        int bestand = Integer.parseInt(m[2]);
                        if (!oldStatus.equals(status)) {
                            if (status.equalsIgnoreCase("Erledigt")) bestand -= qty;
                            else if (status.equalsIgnoreCase("Abgeschlossen")) bestand += qty;
                        }
                        m[2] = String.valueOf(Math.max(bestand, 0));
                    }
                }
                break;
            }
        }

        csv.writeCsv("material.csv", mats);
        csv.writeCsv("bestellungen.csv", rows);
        return "<meta http-equiv='refresh' content='0;url=/admin/bestellungen'>";
    }
}