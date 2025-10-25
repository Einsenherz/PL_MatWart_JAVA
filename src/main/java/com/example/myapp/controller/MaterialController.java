package com.example.myapp.controller;

import com.example.myapp.model.Material;
import com.example.myapp.service.ListeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/material")
public class MaterialController {
    private final ListeService listeService;

    public MaterialController(ListeService listeService) {
        this.listeService = listeService;
    }

    @GetMapping
    @ResponseBody
    public String materialUebersicht() {
        List<Material> mats = listeService.ladeMaterialien();
        StringBuilder html = new StringBuilder();
        html.append("<html><body><h2>Materialübersicht</h2>");
        html.append("<table border='1'><tr><th>ID</th><th>Name</th><th>Bestand</th></tr>");
        for (Material m : mats) {
            html.append("<tr><td>").append(m.getId())
                .append("</td><td>").append(m.getName())
                .append("</td><td>").append(m.getBestand())
                .append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }
}
