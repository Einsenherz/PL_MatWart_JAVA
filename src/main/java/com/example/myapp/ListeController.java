package com.example.myapp;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ListeController {
    private List<String> liste = new ArrayList<>(List.of("Eintrag 1", "Eintrag 2", "Eintrag 3"));
    
    @GetMapping("/")
    public String home() {
    return "Willkommen auf der Website zur Materialverwaltung";
    }
    
    @GetMapping("/liste")
    public List<String> getListe() {
        return liste;
    }

    @PostMapping("/liste/add")
    public void addEintrag(@RequestBody String neuerEintrag) {
        liste.add(neuerEintrag);
    }

    @DeleteMapping("/liste/delete")
    public void deleteEintrag(@PathVariable int index) {
        if (index >= 0 && index < liste.size()) {
            liste.remove(index);
        }
    }
}
