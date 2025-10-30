package com.example.myapp.controller;

import com.example.myapp.model.Benutzer;
import com.example.myapp.service.AuthService;
import com.example.myapp.service.CsvStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class LoginController extends BasePageController {

    @Autowired
    private CsvStorageService csv;

    @Autowired
    private AuthService auth;

    @GetMapping({"/", "/login"})
    public String loginPage() {
        // Thymeleaf-Template "login.html" unter src/main/resources/templates
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session) {

        List<Benutzer> users = csv.readCsv("benutzer.csv").stream()
                .map(Benutzer::fromCsv).collect(Collectors.toList());

        Benutzer user = auth.findByUsername(users, username);
        if (auth.checkPassword(user, password)) {
            session.setAttribute("user", user);
            if (user.isAdmin()) {
                return "<meta http-equiv='refresh' content='0;url=/admin'>";
            } else {
                return "<meta http-equiv='refresh' content='0;url=/home'>";
            }
        }
        return htmlHeader("Login")
                + "<p>Falscher Benutzername oder Passwort.</p>"
                + "<a href='/login'>Zurück</a>"
                + htmlFooter();
    }
}
