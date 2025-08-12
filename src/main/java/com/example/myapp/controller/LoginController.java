package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    private final ListeService service;
    private final String ADMIN_PASS = "8500Dieros";

    public LoginController(ListeService service) {
        this.service = service;
    }

    private String htmlHead(String title) {
        return "<html><head><meta charset='UTF-8'><title>" + title + "</title>"
             + "<link rel='stylesheet' href='/style.css'>"
             + "</head><body>"
             + "<header>"
             + "<img src='/images/Logo_Pfadi_Panthera_Leo.png' alt='Logo' style='height:80px;'>"
             + "<h1>" + title + "</h1>"
             + "</header>";
    }

    @GetMapping("/")
    public String loginPage() {
        return htmlHead("Panthera Leo MatWart Login")
            + "<form method='post' action='/login'>"
            + "<input type='text' name='username' placeholder='Benutzername' required><br><br>"
            + "<input type='password' name='password' placeholder='Passwort' required><br><br>"
            + "<button type='submit'>Bestätigen</button>"
            + "</form>"
            + "</body></html>";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        if ("admin".equals(username) && ADMIN_PASS.equals(password)) {
            session.setAttribute("loggedInUser", "admin");
            return "<meta http-equiv='refresh' content='0; URL=/admin'>";
        } else if (service.checkPasswort(username, password)) {
            session.setAttribute("loggedInUser", username);
            return "<meta http-equiv='refresh' content='0; URL=/normalbenutzer/" + username + "'>";
        } else {
            return htmlHead("Login fehlgeschlagen")
                + "<p>Falsches Passwort!</p>"
                + "<a href='/'>Zurück zum Login</a>"
                + "</body></html>";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "<meta http-equiv='refresh' content='0; URL=/'>";
    }
}
