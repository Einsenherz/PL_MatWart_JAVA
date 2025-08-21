package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.service.ListeService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController extends BasePageController {

    private final ListeService service;

    public AdminController(ListeService service) {
        this.service = service;
    }

    // ===== Startseite Admin =====
    @GetMapping
    public String adminHome() {
        return htmlHeader("Adminbereich")
                + "<form method='get' action='/admin/logins'><button type='submit'>Benutzerverwaltung</button></form>"
                + "<form method='get' action='/admin/listen'><button type='submit'>Bestellungen</button></form>"
                + "<form method='get' action='/admin/archiv'><button type='submit'>Archiv</button></form>"
                + "<form method='get' action='/admin/inventar'><button type='submit'>Inventar</button></form>"
                + "<form method='get' action='/logout'><button class='btn-back' type='submit'>Logout</button></form>"
                + breadcrumb("/admin", "Adminbereich")
                + htmlFooter();
    }

    // ===== Benutzerverwaltung =====
    @GetMapping("/logins")
    public String benutzerListe() {
        List<Benutzer> benutzer = service.getAlleBenutzer();
        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Benutzerverwaltung"));
        html.append("<input type='text' class='table-filter' placeholder='Suche Benutzer...' data-table='benutzerTabelle'>");
        html.append("<table id='benutzerTabelle'><thead><tr><th>Benutzername</th><th>Passwort</th><th>Aktionen</th></tr></thead><tbody>");
        for (Benutzer b : benutzer) {
            html.append("<tr><td>").append(b.getUsername()).append("</td>")
                    .append("<td>").append(b.getPasswort()).append("</td><td>")
                    .append("<form style='display:inline;' method='get' action='/admin/logins/anpassen'>")
                    .append("<input type='hidden' name='username' value='").append(b.getUsername()).append("'>")
                    .append("<button type='submit'>Anpassen</button></form>")
                    .append("<form style='display:inline;' method='post' action='/admin/logins/loeschen' onsubmit='return confirm(\"Benutzer wirklich löschen?\");'>")
                    .append("<input type='hidden' name='username' value='").append(b.getUsername()).append("'>")
                    .append("<button type='submit'>Löschen</button></form></td></tr>");
        }
        html.append("</tbody></table>");
        html.append("<h2>Neuen Benutzer hinzufügen</h2>");
        html.append("<form method='post' action='/admin/logins/add'>Benutzername: <input type='text' name='username' required><br>"
                + "Passwort: <input type='password' name='passwort' required><br>"
                + "<button type='submit'>Hinzufügen</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append(breadcrumb("/admin", "Benutzerverwaltung"));
        html.append(htmlFooter());
        return html.toString();
    }

    @PostMapping("/logins/add")
    public String addBenutzer(@RequestParam String username, @RequestParam String passwort) {
        service.addBenutzer(username, passwort);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @GetMapping("/logins/anpassen")
    public String anpassenForm(@RequestParam String username) {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader("Benutzer anpassen"));
        sb.append("<form method='post' action='/admin/logins/anpassen'>")
                .append("<input type='hidden' name='oldUsername' value='").append(username).append("'>")
                .append("Neuer Benutzername: <input type='text' name='newUsername' value='").append(username).append("' required><br>")
                .append("Neues Passwort: <input type='password' name='newPasswort' required><br>")
                .append("<button type='submit'>Bestätigen</button></form>")
                .append("<form method='get' action='/admin/logins'><button class='btn-back' type='submit'>Abbrechen</button></form>");
        sb.append(breadcrumb("/admin", "Benutzer anpassen"));
        sb.append(htmlFooter());
        return sb.toString();
    }

    @PostMapping("/logins/anpassen")
    public String updateBenutzer(@RequestParam String oldUsername,
                                 @RequestParam String newUsername,
                                 @RequestParam String newPasswort) {
        service.updateBenutzer(oldUsername, newUsername, newPasswort);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    @PostMapping("/logins/loeschen")
    public String loescheBenutzer(@RequestParam String username) {
        service.deleteBenutzer(username);
        return "<script>window.location.href='/admin/logins';</script>";
    }

    // ===== Bestellungen =====
    @GetMapping("/listen")
    public String bestellListe() {
        List<Bestellung> bestellungen = service.getAlleBestellungen().stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .toList();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Bestellungen"));
        html.append("<input type='text' class='table-filter' placeholder='Suche Bestellungen...' data-table='bestellTabelle'>");
        html.append("<table id='bestellTabelle'><thead><tr>"
                + "<th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Status</th>"
                + "<th>Eingabedatum</th><th>Rückgabedatum</th><th>Aktionen</th>"
                + "</tr></thead><tbody>");
        for (Bestellung b : bestellungen) {
            html.append("<tr>")
                    .append("<td>").append(b.getBenutzer()).append("</td>")
                    .append("<td>").append(b.getAnzahl()).append("</td>")
                    .append("<td>").append(b.getMaterial()).append("</td>")
                    .append("<td><form method='post' action='/admin/listen/status'>")
                    .append("<input type='hidden' name='id' value='").append(b.getId()).append("'>")
                    .append("<select name='status'>")
                    .append("<option value='in Bearbeitung'").append("in Bearbeitung".equals(b.getStatus()) ? " selected" : "").append(">in Bearbeitung</option>")
                    .append("<option value='Bestätigt'").append("Bestätigt".equals(b.getStatus()) ? " selected" : "").append(">Bestätigt</option>")
                    .append("<option value='Rückgabe fällig'").append("Rückgabe fällig".equals(b.getStatus()) ? " selected" : "").append(">Rückgabe fällig</option>")
                    .append("</select><button type='submit'>Ändern</button></form></td>")
                    .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                    .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                    .append("<td><form method='post' action='/admin/listen/archivieren' onsubmit='return confirm(\"Bestellung archivieren?\");'>")
                    .append("<input type='hidden' name='id' value='").append(b.getId()).append("'>")
                    .append("<button type='submit'>Archivieren</button></form></td>")
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append(breadcrumb("/admin", "Bestellungen"));
        html.append(htmlFooter());
        return html.toString();
    }

    @PostMapping("/listen/status")
    public String updateStatus(@RequestParam Long id, @RequestParam String status) {
        // Bestand ändert sich nur bei "Archiviert" (Logik steckt in ListeService.updateStatusMitBestand)
        service.updateStatusMitBestand(id, status);
        return "<script>window.location.href='/admin/listen';</script>";
    }

    @PostMapping("/listen/archivieren")
    public String archivieren(@RequestParam Long id) {
        // Archivieren = Status setzen + (falls nicht schon) Bestand verringern
        service.updateStatusMitBestand(id, "Archiviert");
        return "<script>window.location.href='/admin/listen';</script>";
    }

    // ===== Archiv =====
    @GetMapping("/archiv")
    public String archivListe() {
        List<Bestellung> archiv = service.getAlleArchiviertenBestellungenSorted();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Archiv"));
        html.append("<input type='text' class='table-filter' placeholder='Suche im Archiv...' data-table='archivTabelle'>");
        html.append("<table id='archivTabelle'><thead><tr>"
                + "<th>Benutzer</th><th>Anzahl</th><th>Material</th><th>Eingabedatum</th><th>Rückgabedatum</th>"
                + "</tr></thead><tbody>");
        for (Bestellung b : archiv) {
            html.append("<tr>")
                    .append("<td>").append(b.getBenutzer()).append("</td>")
                    .append("<td>").append(b.getAnzahl()).append("</td>")
                    .append("<td>").append(b.getMaterial()).append("</td>")
                    .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                    .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/admin/archiv/export'><button type='submit'>📁 Archiv exportieren (CSV)</button></form>");
        html.append("<form method='get' action='/admin/archiv/export-pdf'><button type='submit'>📄 Archiv exportieren (PDF)</button></form>");
        html.append("<form method='post' action='/admin/archiv/clear' onsubmit='return confirm(\"Wirklich alle archivierten Einträge löschen?\");'><button type='submit'>🗑️ Archiv leeren</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append(breadcrumb("/admin", "Archiv"));
        html.append(htmlFooter());
        return html.toString();
    }

    @GetMapping("/archiv/export")
    public ResponseEntity<byte[]> exportiereArchivAlsCsv() {
        List<Bestellung> archiv = service.getAlleArchiviertenBestellungenSorted();
        StringBuilder csv = new StringBuilder("Benutzer,Anzahl,Material,Eingabedatum,Rueckgabedatum\n");
        for (Bestellung b : archiv) {
            csv.append(b.getBenutzer()).append(',')
                    .append(b.getAnzahl()).append(',')
                    .append(b.getMaterial()).append(',')
                    .append(b.getEingabedatum() != null ? b.getEingabedatum() : "").append(',')
                    .append(b.getRueckgabedatum() != null ? b.getRueckgabedatum() : "")
                    .append('\n');
        }
        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archiv.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvBytes);
    }

    @GetMapping("/archiv/export-pdf")
    public ResponseEntity<byte[]> exportiereArchivAlsPdf() {
        try {
            List<Bestellung> archiv = service.getAlleArchiviertenBestellungenSorted();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Archivierte Bestellungen"));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(5);
            table.addCell("Benutzer");
            table.addCell("Anzahl");
            table.addCell("Material");
            table.addCell("Eingabedatum");
            table.addCell("Rückgabedatum");
            for (Bestellung b : archiv) {
                table.addCell(b.getBenutzer());
                table.addCell(String.valueOf(b.getAnzahl()));
                table.addCell(b.getMaterial());
                table.addCell(b.getEingabedatum() != null ? b.getEingabedatum().toString() : "");
                table.addCell(b.getRueckgabedatum() != null ? b.getRueckgabedatum().toString() : "");
            }
            document.add(table);
            document.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archiv.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/archiv/clear")
    public String archivLeeren() {
        service.leereArchiv();
        return "<script>window.location.href='/admin/archiv';</script>";
    }

    // ===== Inventar =====
    @GetMapping("/inventar")
    public String inventarListe() {
        List<Material> inventar = service.getAlleMaterialien();
        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Inventar"));
        html.append("<input type='text' class='table-filter' placeholder='Suche Material...' data-table='inventarTabelle'>");
        html.append("<table id='inventarTabelle'><thead><tr><th>Name</th><th>Bestand</th></tr></thead><tbody>");
        for (Material m : inventar) {
            html.append("<tr>")
                    .append("<td>").append(m.getName()).append("</td>")
                    .append("<td>").append(m.getBestand()).append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/admin/inventar/export-pdf'><button type='submit'>📄 Inventar exportieren (PDF)</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append(breadcrumb("/admin", "Inventar"));
        html.append(htmlFooter());
        return html.toString();
    }

    @GetMapping("/inventar/export-pdf")
    public ResponseEntity<byte[]> exportiereInventarAlsPdf() {
        try {
            List<Material> inventar = service.getAlleMaterialien();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Inventar - Gesamtbestände"));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(2);
            table.addCell("Name");
            table.addCell("Bestand");
            for (Material m : inventar) {
                table.addCell(m.getName());
                table.addCell(String.valueOf(m.getBestand()));
            }
            document.add(table);
            document.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventar.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
