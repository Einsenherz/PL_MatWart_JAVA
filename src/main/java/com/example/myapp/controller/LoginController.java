package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



@Controller
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
            return "<script>window.location.href='/admin';</script>";
        } else if (service.checkPasswort(username, password)) {
            session.setAttribute("loggedInUser", username);
            return "<script>window.location.href='/normalbenutzer/" + username + "';</script>";
        } else {
            return "<p>Falsches Passwort! <a href='/'>Zurück</a></p>";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "<script>window.location.href='/'</script>";
    }
}
