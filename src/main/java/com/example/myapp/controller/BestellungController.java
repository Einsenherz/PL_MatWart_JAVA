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

        // Daten einlesen
        List<Bestellung> best = csv.readCsv("bestellungen.csv").stream()
                .map(Bestellung::fromCsv)
                .collect(Collectors.toList());

        List<Benutzer> allUsers = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv)
                .collect(Collectors.toList());

        List<Material> mats = csv.readCsv("material.csv").stream()
                .map(Material::fromCsv)
                .collect(Collectors.toList());

        // Filterung (Admin kann Benutzer auswählen)
        if (filterUser != null && !filterUser.isEmpty() && u.isAdmin()) {
            best = best.stream()
                    .filter(b -> b.getBenutzer().equalsIgnoreCase(filterUser))
                    .collect(Collectors.toList());
        } else if (!u.isAdmin()) {
            best = best.stream()
                    .filter(b -> b.getBenutzer().equalsIgnoreCase(u.getUsername()))
                    .collect(Collectors.toList());
        }

        // HTML Aufbau
        StringBuilder sb = new StringBuilder(htmlHeader("Bestellungen"));
        sb.append(breadcrumb(u.isAdmin() ? "Admin" : "Home", "Bestellungen"));

        // Suchfeld (Admin)
        if (u.isAdmin()) {
            sb.append("<form action='/admin/bestellungen' method='get' class='form-inline'>")
              .append("<label>Benutzer filtern: ")
              .append("<input type='text' name='user' list='userlist' placeholder='Benutzername' value='")
              .append(filterUser == null ? "" : escape(filterUser)).append("' />")
              .append("<datalist id='userlist'>");
            for (Benutzer bu : allUsers)
                sb.append("<option value='").append(escape(bu.getUsername())).append("'></option>");
            sb.append("</datalist>")
              .append("<button type='submit'>Filtern</button>")
              .append("<a href='/admin/bestellungen' class='btn-link'>Zurücksetzen</a>")
              .append("</label></form>");
        }

        sb.append("<div class='table-container'><table>")
          .append("<tr><th>ID</th><th>Benutzer</th><th>Material</th><th>Anzahl</th><th>Status</th><th>Eingabezeit</th><th>Updated</th></tr>");

        for (Bestellung b : best) {
            sb.append("<tr>")
              .append("<td data-label='ID'>").append(b.getId()).append("</td>")
              .append("<td data-label='Benutzer'>").append(escape(b.getBenutzer())).append("</td>")
              .append("<td data-label='Material'>").append(escape(b.getMaterial())).append("</td>")
              .append("<td data-label='Anzahl'>").append(b.getAnzahl()).append("</td>")
              .append("<td data-label='Status'>").append(escape(b.getStatus())).append("</td>")
              .append("<td data-label='Eingabezeit'>").append(escape(b.getEingabeZeit())).append("</td>")
              .append("<td data-label='Updated'>").append(escape(b.getUpdated())).append("</td>")
              .append("</tr>");
        }

        sb.append("</table></div>");

        // Formular: Bestellung hinzufügen (nur User)
        if (!u.isAdmin()) {
            sb.append("<h3>Neue Bestellung</h3>");
            sb.append("<form action='/bestellungen/add' method='post'>");

            // Mehrere Materialien mit Mengen
            sb.append("<div id='multiItems'>");
            sb.append(renderMaterialSelect(mats, 1));
            sb.append("</div>");
            sb.append("<button type='button' onclick='addItem()'>+ weiteres Material</button><br><br>");
            sb.append("<button type='submit'>Bestellen</button>");
            sb.append("</form>");

            // JS zur dynamischen Materialauswahl
            sb.append("<script>")
              .append("function addItem(){const d=document.getElementById('multiItems');const c=d.children.length+1;")
              .append("d.insertAdjacentHTML('beforeend',`").append(renderMaterialSelect(mats, -1).replace("`", "\\`")).append("`);}")
              .append("</script>");
        }

        // Formular: Bestellung löschen
        sb.append("<h3>Bestellung löschen</h3>");
        sb.append("<form action='/bestellungen/delete' method='post'>")
          .append("ID: <input type='number' name='id' required> ")
          .append("<button type='submit' style='background:#b22'>Löschen</button>")
          .append("</form>");

        // Formular: Status ändern (nur Admin)
        if (u.isAdmin()) {
            sb.append("<h3>Bestellstatus ändern</h3>");
            sb.append("<form action='/bestellungen/status' method='post'>")
              .append("ID: <input type='number' name='id' required> ")
              .append("Status: <select name='status'>")
              .append("<option value='Offen'>Offen</option>")
              .append("<option value='Erledigt'>Erledigt</option>")
              .append("<option value='Abgeschlossen'>Abgeschlossen</option>")
              .append("</select>")
              .append("<button type='submit'>Aktualisieren</button>")
              .append("</form>");
        }

        sb.append("<nav><a href='").append(u.isAdmin() ? "/admin" : "/home").append("'>Zurück</a></nav>");
        sb.append(htmlFooter());
        return sb.toString();
    }

    // Mehrfach-Materialauswahl mit Max-Bestellmenge
    private String renderMaterialSelect(List<Material> mats, int index) {
        StringBuilder s = new StringBuilder();
        s.append("<div class='material-line'>");
        s.append("Material: <input list='mats' name='material' required placeholder='Material suchen...'> ");
        s.append("<datalist id='mats'>");
        for (Material m : mats) {
            s.append("<option value='").append(escape(m.getName()))
             .append("' data-max='").append(m.getBestand()).append("'>")
             .append("</option>");
        }
        s.append("</datalist>");
        s.append("Anzahl: <input type='number' name='anzahl' min='1' required>");
        s.append("</div>");
        return s.toString();
    }

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
            String mat = material.get(i);
            int qty = anzahl.get(i);
            rows.add(new String[]{
                    String.valueOf(id++),
                    u.getUsername(),
                    mat,
                    String.valueOf(qty),
                    "Offen",
                    fmt.format(now),
                    fmt.format(now)
            });
        }

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
        List<String[]> mats = csv.readCsv("material.csv");

        for (String[] r : rows) {
            if (Integer.parseInt(r[0]) == id) {
                String oldStatus = r[4];
                r[4] = status;
                r[6] = fmt.format(LocalDateTime.now()); // Updated

                // Materialbestand anpassen
                String matName = r[2];
                int qty = Integer.parseInt(r[3]);
                for (String[] m : mats) {
                    if (m[0].equalsIgnoreCase(matName)) {
                        int bestand = Integer.parseInt(m[2]);
                        if (!oldStatus.equals(status)) {
                            if (status.equalsIgnoreCase("Erledigt"))
                                bestand -= qty;
                            else if (status.equalsIgnoreCase("Abgeschlossen"))
                                bestand += qty;
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