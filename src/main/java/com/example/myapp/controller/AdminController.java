package com.example.myapp.controller;

import com.example.myapp.model.*;
import com.example.myapp.service.ListeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ListeService listeService;

    public AdminController(ListeService listeService) {
        this.listeService = listeService;
    }

    @GetMapping
    @ResponseBody
    public String adminHome() {
        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Admin-Bereich"));
        html.append(breadcrumb("Admin", "/admin"));
        html.append("<h2>Verwaltung</h2>");
        html.append("<ul>");
        html.append("<li><a href='/admin/materialien'>Materialien</a></li>");
        html.append("<li><a href='/admin/benutzer'>Benutzer</a></li>");
        html.append("<li><a href='/admin/bestellungen'>Bestellungen</a></li>");
        html.append("</ul>");
        html.append(htmlFooter());
        return html.toString();
    }

    @GetMapping("/materialien")
    @ResponseBody
    public String materialListe() {
        List<Material> mats = listeService.ladeMaterialien();
        StringBuilder html = new StringBuilder();
        html.append(htmlHeader("Materialliste"));
        html.append("<table border='1'><tr><th>ID</th><th>Name</th><th>Bestand</th></tr>");
        for (Material m : mats) {
            html.append("<tr><td>").append(m.getId())
                .append("</td><td>").append(escape(m.getName()))
                .append("</td><td>").append(m.getBestand())
                .append("</td></tr>");
        }
        html.append("</table>");
        html.append(htmlFooter());
        return html.toString();
    }

    // --- Utility-Methoden ---
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
}
