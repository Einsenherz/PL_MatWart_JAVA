package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/normalbenutzer")
public class BenutzerController {
    private final ListeService service;

    public BenutzerController(ListeService service) {
        this.service = service;
    }

    @GetMapping("/{benutzer}")
    public String normalBenutzerPage(@PathVariable String benutzer) {
        return service.generiereBenutzerSeite(benutzer);
    }

    @PostMapping("/{benutzer}/bestellen")
    public String bestellung(@PathVariable String benutzer, @RequestParam int anzahl, @RequestParam String material) {
        service.bestelle(benutzer, anzahl, material);
        return "<script>window.location.href='/normalbenutzer/" + benutzer + "';</script>";
    }

    @PostMapping("/{benutzer}/senden")
    public String senden(@PathVariable String benutzer) {
        service.markiereAlsAbgegeben(benutzer);
        return "<script>window.location.href='/normalbenutzer/" + benutzer + "';</script>";
    }
}
