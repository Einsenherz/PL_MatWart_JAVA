package com.example.myapp.controller;

import com.example.myapp.service.ListeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController extends BasePageController {

    private final ListeService service;

    public LoginController(ListeService service) {
        this.service = service;
    }

    @GetMapping("/")
    @ResponseBody
    public String landing() {
        return htmlHeader("Login")
            + "<form method='post' action='/login'>"
            + "Benutzer: <input type='text' name='username' required> "
            + "Passwort: <input type='password' name='passwort' required> "
            + "<button class='btn' type='submit'>Anmelden</button></form>"
            + "<div class='notice'>Standard-Admin: <code>admin / admin</code></div>"
            + htmlFooter();
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String passwort, HttpSession session) {
        if (service.validateLogin(username, passwort)) {
            session.setAttribute("user", username);
            return "redirect:/admin";
        }
        return "redirect:/";
    }
}
