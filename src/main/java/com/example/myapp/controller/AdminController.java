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
import org.springframework.stereotype.Controller;
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
    @ResponseBody
    public String adminHome() {
        return htmlHeader("Adminbereich")
                + "<form method='get' action='/admin/logins'><button class='btn' type='submit'>Benutzerverwaltung</button></form>"
                + "<form method='get' action='/admin/listen'><button class='btn' type='submit'>Bestellungen</button></form>"
                + "<form method='get' action='/admin/archiv'><button class='btn' type='submit'>Archiv</button></form>"
                + "<form method='get' action='/admin/inventar'><button class='btn' type='submit'>Inventar</button></form>"
                + "<form method='get' action='/'><button class='btn-back' type='submit'>Logout</button></form>"
                + breadcrumb("/admin", "Adminbereich")
                + htmlFooter();
    }

    // ===== Benutzerverwaltung =====
    @GetMapping("/logins")
    @ResponseBody
    public String benutzerListe() {
        List<Benutzer> benutzer = service.getAlleBenutzer();
        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Benutzerverwaltung"));
        html.append("<input type='text' class='table-filter' placeholder='Suche Benutzer...' data-table='benutzerTabelle'>");
        html.append("<table id='benutzerTabelle'><thead><tr><th>Benutzername</th><th>Passwort</th><th>Aktionen</th></tr></thead><tbody>");
        for (Benutzer b : benutzer) {
            html.append("<tr><td>").append(escape(b.getUsername())).append("</td>")
                .append("<td>").append(escape(b.getPasswort())).append("</td><td>")
                .append("<form style='display:inline;' method='get' action='/admin/logins/anpassen'>")
                .append("<input type='hidden' name='username' value='").append(escape(b.getUsername())).append("'>")
                .append("<button class='btn' type='submit'>Anpassen</button></form>")
                .append("<form style='display:inline;' method='post' action='/admin/logins/loeschen' onsubmit='return confirm(\"Benutzer wirklich löschen?\");'>")
                .append("<input type='hidden' name='username' value='").append(escape(b.getUsername())).append("'>")
                .append("<button class='btn-danger' type='submit'>Löschen</button></form></td></tr>");
        }
        html.append("</tbody></table>");
        html.append("<h2>Neuen Benutzer hinzufügen</h2>");
        html.append("<form method='post' action='/admin/logins/add'>Benutzername: <input type='text' name='username' required> "
                + "Passwort: <input type='password' name='passwort' required> "
                + "<button class='btn' type='submit'>Hinzufügen</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append(breadcrumb("/admin", "Benutzerverwaltung"));
        html.append(htmlFooter());
        return html.toString();
    }

    @GetMapping("/logins/add")
    public String addBenutzerGetFallback() { return "redirect:/admin/logins"; }

    @PostMapping("/logins/add")
    public String addBenutzer(@RequestParam String username, @RequestParam String passwort) {
        service.addBenutzer(username, passwort);
        return "redirect:/admin/logins";
    }

    @GetMapping("/logins/anpassen")
    @ResponseBody
    public String anpassenForm(@RequestParam String username) {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader("Benutzer anpassen"));
        sb.append("<form method='post' action='/admin/logins/anpassen'>")
          .append("<input type='hidden' name='oldUsername' value='").append(escape(username)).append("'>")
          .append("Neuer Benutzername: <input type='text' name='newUsername' value='").append(escape(username)).append("' required> ")
          .append("Neues Passwort: <input type='password' name='newPasswort' required> ")
          .append("<button class='btn' type='submit'>Bestätigen</button></form>")
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
        return "redirect:/admin/logins";
    }

    @PostMapping("/logins/loeschen")
    public String loescheBenutzer(@RequestParam String username) {
        service.deleteBenutzer(username);
        return "redirect:/admin/logins";
    }

    // ===== Bestellungen =====
    @GetMapping("/listen")
    @ResponseBody
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
                .append("<td>").append(escape(b.getBenutzer())).append("</td>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(escape(b.getMaterial())).append("</td>")
                .append("<td><form method='post' action='/admin/listen/status'>")
                .append("<input type='hidden' name='id' value='").append(b.getId()).append("'>")
                .append("<select name='status'>")
                .append("<option value='in Bearbeitung'").append("in Bearbeitung".equals(b.getStatus()) ? " selected" : "").append(">in Bearbeitung</option>")
                .append("<option value='Bestätigt'").append("Bestätigt".equals(b.getStatus()) ? " selected" : "").append(">Bestätigt</option>")
                .append("<option value='Rückgabe fällig'").append("Rückgabe fällig".equals(b.getStatus()) ? " selected" : "").append(">Rückgabe fällig</option>")
                .append("<option value='Archiviert'").append("Archiviert".equals(b.getStatus()) ? " selected" : "").append(">Archiviert</option>")
                .append("</select><button class='btn' type='submit'>Ändern</button></form></td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("<td><form method='post' action='/admin/listen/archivieren' onsubmit='return confirm(\"Bestellung archivieren?\");'>")
                .append("<input type='hidden' name='id' value='").append(b.getId()).append("'>")
                .append("<button class='btn-secondary' type='submit'>Archivieren</button></form></td>")
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
        service.updateStatusMitBestand(id, status);
        return "redirect:/admin/listen";
    }

    @PostMapping("/listen/archivieren")
    public String archivieren(@RequestParam Long id) {
        service.updateStatusMitBestand(id, "Archiviert");
        return "redirect:/admin/listen";
    }

    // ===== Archiv =====
    @GetMapping("/archiv")
    @ResponseBody
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
                .append("<td>").append(escape(b.getBenutzer())).append("</td>")
                .append("<td>").append(b.getAnzahl()).append("</td>")
                .append("<td>").append(escape(b.getMaterial())).append("</td>")
                .append("<td>").append(b.getEingabedatum() != null ? b.getEingabedatum().format(dtf) : "").append("</td>")
                .append("<td>").append(b.getRueckgabedatum() != null ? b.getRueckgabedatum().format(dtf) : "").append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/admin/archiv/export'><button class='btn' type='submit'>📁 Archiv exportieren (CSV)</button></form>");
        html.append("<form method='get' action='/admin/archiv/export-pdf'><button class='btn' type='submit'>📄 Archiv exportieren (PDF)</button></form>");
        html.append("<form method='post' action='/admin/archiv/clear' onsubmit='return confirm(\"Wirklich alle archivierten Einträge löschen?\");'><button class='btn-danger' type='submit'>🗑️ Archiv leeren</button></form>");
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
        return "redirect:/admin/archiv";
    }

    // ===== Inventar =====
    @GetMapping("/inventar")
    @ResponseBody
    public String inventarListe() {
        List<Material> inventar = service.getAlleMaterialien();
        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Inventar"));

        // Toolbar oben, mittig vor Export-Button
        html.append("<div class='toolbar'>")
            .append("<form method='get' action='/admin/inventar/neu' style='display:inline;'>")
            .append("<button class='btn' type='submit'>➕ Neues Material</button></form>")
            .append("<form method='get' action='/admin/inventar/bearbeiten' style='display:inline;'>")
            .append("<select name='id'>");
        for (Material m : inventar) {
            html.append("<option value='").append(m.getId()).append("'>")
               .append(escape(m.getName())).append(" (").append(m.getBestand()).append(")")
               .append("</option>");
        }
        html.append("</select><button class='btn' type='submit'>✏️ Bearbeiten</button></form>")
            .append("<form method='post' action='/admin/inventar/loeschen' style='display:inline;' onsubmit='return confirm(\"Material wirklich löschen?\");'>")
            .append("<select name='id'>");
        for (Material m : inventar) {
            html.append("<option value='").append(m.getId()).append("'>")
               .append(escape(m.getName())).append(" (").append(m.getBestand()).append(")")
               .append("</option>");
        }
        html.append("</select><button class='btn-danger' type='submit'>🗑️ Löschen</button></form>")
            .append("</div>");

        html.append("<input type='text' class='table-filter' placeholder='Suche Material...' data-table='inventarTabelle'>");
        html.append("<table id='inventarTabelle'><thead><tr><th>Name</th><th>Bestand</th></tr></thead><tbody>");
        for (Material m : inventar) {
            html.append("<tr>")
                .append("<td>").append(escape(m.getName())).append("</td>")
                .append("<td>").append(m.getBestand()).append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<form method='get' action='/admin/inventar/export-pdf'><button class='btn' type='submit'>📄 Inventar exportieren (PDF)</button></form>");
        html.append("<form method='get' action='/admin'><button class='btn-back' type='submit'>Zurück</button></form>");
        html.append(breadcrumb("/admin", "Inventar"));
        html.append(htmlFooter());
        return html.toString();
    }

    @GetMapping("/inventar/neu")
    @ResponseBody
    public String neuesMaterialForm() {
        return htmlHeader("Neues Material")
            + "<form method='post' action='/admin/inventar/neu'>"
            + "Name: <input type='text' name='name' required> "
            + "Bestand: <input type='number' name='bestand' min='0' value='0' required> "
            + "<button class='btn' type='submit'>Speichern</button></form>"
            + "<form method='get' action='/admin/inventar'><button class='btn-back' type='submit'>Abbrechen</button></form>"
            + htmlFooter();
    }

    @PostMapping("/inventar/neu")
    public String neuesMaterial(@RequestParam String name, @RequestParam int bestand) {
        service.createMaterial(name, bestand);
        return "redirect:/admin/inventar";
    }

    @GetMapping("/inventar/bearbeiten")
    @ResponseBody
    public String bearbeitenMaterialForm(@RequestParam Long id) {
        Material m = service.getAlleMaterialien().stream().filter(x -> x.getId().equals(id)).findFirst()
                .orElse(null);
        if (m == null) return "redirect:/admin/inventar";
        return htmlHeader("Material bearbeiten")
            + "<form method='post' action='/admin/inventar/bearbeiten'>"
            + "<input type='hidden' name='id' value='"+id+"'>"
            + "Name: <input type='text' name='name' value='"+escape(m.getName())+"' required> "
            + "Bestand: <input type='number' name='bestand' min='0' value='"+m.getBestand()+"' required> "
            + "<button class='btn' type='submit'>Speichern</button></form>"
            + "<form method='get' action='/admin/inventar'><button class='btn-back' type='submit'>Abbrechen</button></form>"
            + htmlFooter();
    }

    @PostMapping("/inventar/bearbeiten")
    public String bearbeitenMaterial(@RequestParam Long id, @RequestParam String name, @RequestParam int bestand) {
        service.updateMaterial(id, name, bestand);
        return "redirect:/admin/inventar";
    }

    @PostMapping("/inventar/loeschen")
    public String loeschenMaterial(@RequestParam Long id) {
        service.deleteMaterial(id);
        return "redirect:/admin/inventar";
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

private String htmlHeader(String title) {
    return "<html><head><title>" + escape(title) + "</title></head><body><h1>" + escape(title) + "</h1>";
}

private String htmlFooter() {
    return "</body></html>";
}

private String breadcrumb(String name, String link) {
    return "<nav><a href='/admin'>Admin</a> / <a href='" + link + "'>" + escape(name) + "</a></nav>";
}

private String escape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
}

