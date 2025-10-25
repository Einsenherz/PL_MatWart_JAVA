package com.example.myapp.controller;

import com.example.myapp.model.Material;
import com.example.myapp.service.MaterialService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/materialien")
public class MaterialController {
    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public String liste(Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("materialien", materialService.getAlle());
        model.addAttribute("neuesMaterial", new Material());
        return "materialien";
    }

    @PostMapping("/hinzufuegen")
    public String hinzufuegen(@ModelAttribute Material neuesMaterial) {
        materialService.hinzufuegen(neuesMaterial);
        return "redirect:/admin/materialien";
    }

    @PostMapping("/loeschen")
    public String loeschen(@RequestParam String name) {
        materialService.loeschen(name);
        return "redirect:/admin/materialien";
    }
}
