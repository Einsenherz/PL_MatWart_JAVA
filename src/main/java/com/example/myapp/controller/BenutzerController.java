package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.ListeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für Benutzerverwaltung.
 * ACHTUNG: In dieser Datei darf KEINE öffentliche Klasse "Benutzer" definiert sein.
 * Verwende stattdessen die Model-Klasse com.example.myapp.model.Benutzer (Import oben).
 */
@Controller
@RequestMapping("/api/benutzer")
public class BenutzerController {

    private final ListeService listeService;

    public BenutzerController(ListeService listeService) {
        this.listeService = listeService;
    }

    @GetMapping
    @ResponseBody
    public List<Benutzer> list() {
        return listeService.getAlleBenutzer();
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestParam String username,
                                       @RequestParam String passwort) {
        listeService.addBenutzer(username, passwort);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestParam String oldUsername,
                                       @RequestParam String newUsername,
                                       @RequestParam String newPasswort) {
        listeService.updateBenutzer(oldUsername, newUsername, newPasswort);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String username) {
        listeService.deleteBenutzer(username);
        return ResponseEntity.noContent().build();
    }
}
