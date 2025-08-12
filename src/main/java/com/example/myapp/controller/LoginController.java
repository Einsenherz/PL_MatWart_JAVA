package com.example.myapp.controller;

import com.example.myapp.service.ListeService;

import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {
    private final ListeService service;

    public LoginController(ListeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String loginForm() {
        return "<html><head><title>Login</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "</head><body>"
                + "<header><h1>Materialverwaltung Login</h1></header>"
                + "<main>"
                + "<form method='post' action='/login'>"
                + "Benutzername: <input type='text' name='username' required><br>"
                + "Passwort: <input type='password' name='passwort' required><br>"
                + "<button type='submit'>Anmelden</button></form>"
                + "</main></body></html>";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String passwort) {
        if (service.isValidAdmin(username, passwort)) {
            return "<script>window.location.href='/admin';</script>";
        } else if (service.isValidBenutzer(username, passwort)) {
            return "<script>window.location.href='/benutzer?username=" + username + "';</script>";
        } else {
            return "<html><head><title>Login fehlgeschlagen</title>"
                    + "<link rel='stylesheet' href='/style.css'>"
                    + "</head><body>"
                    + "<header><h1>Login fehlgeschlagen</h1></header>"
                    + "<main>"
                    + "<p>Benutzername oder Passwort falsch.</p>"
                    + "<form method='get' action='/'><button class='btn-back' type='submit'>Zurück</button></form>"
                    + "</main></body></html>";
        }
    }
}
