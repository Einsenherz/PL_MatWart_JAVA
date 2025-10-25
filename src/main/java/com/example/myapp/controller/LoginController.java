package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.ListeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class LoginController {
    private final ListeService listeService;

    public LoginController(ListeService listeService) {
        this.listeService = listeService;
    }

    @GetMapping("/login")
    @ResponseBody
    public String loginPage() {
        return "<html><body><form action='/login' method='post'>" +
                "Benutzername: <input name='username'><br>" +
                "Passwort: <input type='password' name='password'><br>" +
                "<input type='submit' value='Login'></form></body></html>";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        if (username.equals("admin") && password.equals("admin123")) {
            return "redirect:/admin";
        }
        List<Benutzer> benutzerListe = listeService.ladeBenutzer();
        for (Benutzer b : benutzerListe) {
            if (b.getUsername().equals(username) && b.getPassword().equals(password)) {
                return "redirect:/user_home";
            }
        }
        return "redirect:/login?error=true";
    }
}
