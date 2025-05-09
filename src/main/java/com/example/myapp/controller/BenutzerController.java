package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/normalbenutzer")
public class BenutzerController {

    private final ListeService service;

    public BenutzerController(ListeService service) {
        this.service = service;
    }

    @GetMapping("/{benutzer}")
    @ResponseBody
    public String normalBenutzerPage(@PathVariable String benutzer, HttpSession session) {
        String loggedUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedUser)) {
            return "<script>window.location.href='/'</script>";
        }
        return service.generiereBenutzerSeite(benutzer);
    }

    @PostMapping("/{benutzer}/bestellen")
    public String bestellung(@PathVariable String benutzer, @RequestParam int anzahl, @RequestParam String material, HttpSession session) {
        String loggedUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedUser)) {
            return "redirect:/";
        }
        service.bestelle(benutzer, anzahl, material);
        return "redirect:/normalbenutzer/" + benutzer;
    }

    @PostMapping("/{benutzer}/senden")
    public String senden(@PathVariable String benutzer, HttpSession session) {
        String loggedUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedUser)) {
            return "redirect:/";
        }
        service.markiereAlsAbgegeben(benutzer);
        return "redirect:/normalbenutzer/" + benutzer;
    }

    @PostMapping("/{benutzer}/loeschen/{id}")
    public String loeschen(@PathVariable String benutzer, @PathVariable Long id, HttpSession session) {
        String loggedUser = (String) session.getAttribute("loggedInUser");
        if (!benutzer.equals(loggedUser)) {
            return "redirect:/";
        }
        service.loescheBestellungWennMoeglich(id);
        return "redirect:/normalbenutzer/" + benutzer;
    }
}
