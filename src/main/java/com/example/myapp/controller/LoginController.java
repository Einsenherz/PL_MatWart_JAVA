package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController extends BasePageController {

    private final ListeService service;

    public LoginController(ListeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String loginForm() {
        return htmlHeader("Login")
                + "<form method='post' action='/login' class='styled-form'>"
                + "<label>Benutzername:</label> <input type='text' name='username' required>"
                + "<label>Passwort:</label> <input type='password' name='passwort' required>"
                + "<br><button type='submit'>Anmelden</button>"
                + "</form>"
                + breadcrumb("/", "Login")
                + htmlFooter();
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String passwort,
                        HttpSession session) {
        String role = service.checkLogin(username, passwort); // "admin" | "benutzer" | null
        if (role == null) {
            return "<script>alert('Ungültige Login-Daten!');window.location.href='/';</script>";
        }
        session.setAttribute("username", username);
        session.setAttribute("role", role); // WICHTIG: exakt "role"

        if ("admin".equals(role)) {
            return "<script>window.location.href='/admin';</script>";
        } else {
            return "<script>window.location.href='/benutzer';</script>";
        }
    }
}
