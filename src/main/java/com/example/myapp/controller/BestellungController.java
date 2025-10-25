package com.example.myapp.controller;

import com.example.myapp.model.Bestellung;
import com.example.myapp.service.BestellungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BestellungController {
    private final BestellungService bestellungService;

    public BestellungController(BestellungService bestellungService) {
        this.bestellungService = bestellungService;
    }

    // Benutzeransicht
    @GetMapping("/user/bestellungen")
    public String userBestellungen(HttpSession session, Model model) {
        if (!"USER".equals(session.getAttribute("role"))) return "redirect:/";
        String username = (String) session.getAttribute("username");
        model.addAttribute("bestellungen", bestellungService.getVonBenutzer(username));
        model.addAttribute("neueBestellung", new Bestellung());
        return "user_bestellungen";
    }

    @PostMapping("/user/bestellungen/hinzufuegen")
    public String hinzufuegen(@ModelAttribute Bestellung neueBestellung, HttpSession session) {
        String username = (String) session.getAttribute("username");
        neueBestellung.setBenutzername(username);
        neueBestellung.setStatus("Offen");
        bestellungService.hinzufuegen(neueBestellung);
        return "redirect:/user/bestellungen";
    }

    // Adminansicht
    @GetMapping("/admin/bestellungen")
    public String adminBestellungen(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("bestellungen", bestellungService.getAlle());
        return "admin_bestellungen";
    }

    @PostMapping("/admin/bestellungen/status")
    public String statusAendern(@RequestParam int index, @RequestParam String status) {
        bestellungService.statusAendern(index, status);
        return "redirect:/admin/bestellungen";
    }

    @PostMapping("/admin/bestellungen/loeschen")
    public String loeschen(@RequestParam int index) {
        bestellungService.loeschen(index);
        return "redirect:/admin/bestellungen";
    }
}
