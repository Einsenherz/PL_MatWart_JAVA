package com.example.myapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController extends BasePageController {

    @GetMapping("/home")
    public String home(HttpSession session) {
        // einfache Gatekeeper-Logik (optional): wenn nicht eingeloggt -> Login
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "user_home"; // Thymeleaf-Template
    }
}
