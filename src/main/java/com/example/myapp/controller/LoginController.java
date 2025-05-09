package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController  // WICHTIG: @RestController statt @Controller für direkte String-Rückgabe
public class LoginController {
    private final ListeService service;
    private final String ADMIN_PASS = "8500Dieros";

    public LoginController(ListeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String loginPage() {
        return """
            <html><head><title>Login</title><style>
            body { text-align: center; margin-top: 100px; font-family: Arial; }
            input, button { font-size: 16px; margin: 5px; }
            </style></head><body>
            <h1>Panthera Leo MatWart Login</h1>
            <form method='post' action='/login'>
                <input type='text' name='username' placeholder='Benutzername' required><br><br>
                <input type='password' name='password' placeholder='Passwort' required><br><br>
                <button type='submit'>Bestätigen</button>
            </form>
            </body></html>
        """;
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
            return """
                <html><head><title>Login fehlgeschlagen</title></head><body>
                <p>Falsches Passwort!</p>
                <a href='/'>Zurück zum Login</a>
                </body></html>
            """;
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "<meta http-equiv='refresh' content='0; URL=/'>";
    }
}
