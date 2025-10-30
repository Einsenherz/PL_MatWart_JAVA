package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @GetMapping
    @ResponseBody
    public String dashboard(HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null || !u.isAdmin()) {
            return "Nicht eingeloggt oder keine Berechtigung";
        }

        List<Material> mats = csv.readCsv("material.csv").stream()
                .map(Material::fromCsv).collect(Collectors.toList());
        List<Benutzer> users = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv).collect(Collectors.toList());
        List<Bestellung> bests = csv.readCsv("bestellungen.csv").stream()
                .map(Bestellung::fromCsv).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder(htmlHeader("Admin-Dashboard"));
        sb.append(breadcrumb("Admin", "Übersicht"));
        sb.append("<h2>Admin Übersicht</h2><ul>")
                .append("<li>Benutzer: ").append(users.size()).append("</li>")
                .append("<li>Materialien: ").append(mats.size()).append("</li>")
                .append("<li>Bestellungen: ").append(bests.size()).append("</li>")
                .append("</ul>");
        sb.append("<nav>")
                .append("<a href='/admin/benutzer'>Benutzerverwaltung</a> | ")
                .append("<a href='/admin/bestellungen'>Bestellungen</a>")
                .append("</nav>");
        sb.append("<tr>")
                 .append("<td data-label='ID'>").append(b.getId()).append("</td>")
                 .append("<td data-label='Benutzer'>").append(escape(b.getBenutzer())).append("</td>")
                 .append("<td data-label='Material'>").append(escape(b.getMaterial())).append("</td>")
                 .append("<td data-label='Anzahl'>").append(b.getAnzahl()).append("</td>")
                 .append("<td data-label='Status'>").append(escape(b.getStatus())).append("</td>")
                 .append("</tr>");
        sb.append(htmlFooter());
        return sb.toString();
    }
}