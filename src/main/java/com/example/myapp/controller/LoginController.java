package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    private final ListeService service;

    public LoginController(ListeService service) {
        this.service = service;
    }

    private String breadcrumb(String path) {
        return "<div class='breadcrumb'><a href='/'>Home</a> > " + path + "</div>";
    }

    @GetMapping("/")
    public String loginForm() {
        return "<html><head><title>Login</title>"
                + "<link rel='stylesheet' href='/style.css'>"
                + "<script src='/script.js'></script>"
                + "</head><body>"
                + "<header><h1>Login</h1></header>"
                + "<main class='centered-content'>"
                + "<form method='post' action='/login' class='styled-form'>"
                + "<label>Benutzername:</label> <input type='text' name='username' required><br>"
                + "<label>Passwort:</label> <input type='password' name='passwort' required><br><br>"
                + "<button type='submit'>Anmelden</button>"
                + "</form>"
                + "</main>"
                + breadcrumb("Login")
                + "</body></html>";
    }

    @PostMapping("/login")
public String login(@RequestParam String username, @RequestParam String passwort, HttpSession session) {
    String rolle = service.checkLogin(username, passwort);
    if (rolle != null) {
        session.setAttribute("username", username);
        session.setAttribute("rolle", rolle);
        if ("admin".equals(rolle)) {
            return "<script>window.location.href='/admin';</script>";
        } else {
            return "<script>window.location.href='/benutzer';</script>";
        }
    } else {
        return "<script>alert('Ungültige Login-Daten!');window.location.href='/';</script>";
    }
}

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "<script>alert('Erfolgreich ausgeloggt!');window.location.href='/';</script>";
    }
}
