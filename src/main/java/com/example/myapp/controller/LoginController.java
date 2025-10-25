package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.BenutzerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {
    private final BenutzerService benutzerService;

    public LoginController(BenutzerService benutzerService) {
        this.benutzerService = benutzerService;
    }

    @GetMapping("/")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String passwort, HttpSession session) {
        if (benutzerService.istAdmin(username, passwort)) {
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin";
        } else if (benutzerService.existiertBenutzer(username, passwort)) {
            session.setAttribute("role", "USER");
            session.setAttribute("username", username);
            return "redirect:/user";
        } else {
            return "redirect:/?error";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
