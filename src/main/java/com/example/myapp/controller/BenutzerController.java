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
@RequestMapping("/benutzer")
public class BenutzerController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @GetMapping
    @ResponseBody
    public String list(HttpSession session) {
        Benutzer u = (Benutzer) session.getAttribute("user");
        if (u == null || !u.isAdmin()) return "Zugriff verweigert";

        List<Benutzer> users = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder(htmlHeader("Benutzerverwaltung"));
        sb.append(breadcrumb("Admin", "Benutzer"));
        sb.append("<table><tr><th>Benutzername</th><th>Admin</th></tr>");
        for (Benutzer b : users)
            sb.append("<tr><td>").append(escape(b.getUsername())).append("</td><td>")
                    .append(b.isAdmin() ? "Ja" : "Nein").append("</td></tr>");
        sb.append("</table><a href='/admin'>Zurück</a>");
        sb.append(htmlFooter());
        return sb.toString();
    }
}
