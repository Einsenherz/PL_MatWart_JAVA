package com.example.myapp.controller;

import com.example.myapp.model.*;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bestellungen")
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
        sb.append(breadcrumb("Home", "Bestellungen"));
        sb.append("<table><tr><th>ID</th><th>Benutzer</th><th>Material</th><th>Anzahl</th><th>Status</th></tr>");
        for (Bestellung b : best)
            if (u.isAdmin() || b.getBenutzer().equals(u.getUsername()))
                sb.append("<tr><td>").append(b.getId()).append("</td><td>")
                        .append(escape(b.getBenutzer())).append("</td><td>")
                        .append(escape(b.getMaterial())).append("</td><td>")
                        .append(b.getAnzahl()).append("</td><td>")
                        .append(escape(b.getStatus())).append("</td></tr>");
        sb.append("</table><a href='/home'>Zurück</a>");
        sb.append(htmlFooter());
        return sb.toString();
    }
}
