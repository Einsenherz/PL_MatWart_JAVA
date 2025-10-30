package com.example.myapp.controller;

import com.example.myapp.model.Material;
import com.example.myapp.model.Benutzer;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/material")
public class MaterialController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @GetMapping
    @ResponseBody
    public String list(HttpSession session) {
        if (session.getAttribute("user") == null) return "Nicht eingeloggt";

        List<Material> mats = csv.readCsv("material.csv").stream()
                .map(Material::fromCsv).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder(htmlHeader("Materialübersicht"));
        sb.append(breadcrumb("Home", "Material"));
        sb.append("<table><tr><th>ID</th><th>Name</th><th>Bestand</th></tr>");
        for (Material m : mats) {
            sb.append("<tr><td>").append(m.getId()).append("</td><td>")
              .append(escape(m.getName())).append("</td><td>")
              .append(m.getBestand()).append("</td></tr>");
        }
        sb.append("</table><a href='/home'>Zurück</a>");
        sb.append(htmlFooter());
        return sb.toString();
    }
}
