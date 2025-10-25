package com.example.myapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasePageController {

    @GetMapping("/admin")
    public String adminHome(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        return "admin_home";
    }

    @GetMapping("/user")
    public String userHome(HttpSession session) {
        if (!"USER".equals(session.getAttribute("role"))) return "redirect:/";
        return "user_home";
    }
}
