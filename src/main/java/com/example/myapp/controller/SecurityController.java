package com.example.myapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SecurityController sichert automatisch Admin- und Benutzer-Routen.
 * Liegt im Controller-Paket und prüft jede Anfrage auf gültige Session.
 * Hardcodierter Admin: Benutzername "admin" + Passwort "Dieros8500"
 */
@Component
public class SecurityController implements HandlerInterceptor, WebMvcConfigurer {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "Dieros8500";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // ==== Kein Login vorhanden ====
        if (session == null || session.getAttribute("username") == null) {
            // Freie Routen durchlassen
            if (isPublicRoute(uri)) return true;
            redirectLogin(response, "Bitte zuerst einloggen!");
            return false;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String password = (String) session.getAttribute("password");

        // ==== Admin-Bereich ====
        if (uri.startsWith("/admin")) {
            if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
                return true;
            }
            redirectLogin(response, "Keine Berechtigung für den Adminbereich!");
            return false;
        }

        // ==== Benutzer-Bereich ====
        if (uri.startsWith("/benutzer")) {
            if ("benutzer".equals(role)) {
                return true;
            }
            redirectLogin(response, "Keine Berechtigung für den Benutzerbereich!");
            return false;
        }

        return true; // andere Routen zulassen
    }

    private boolean isPublicRoute(String uri) {
        return uri.equals("/") || uri.equals("/login") || uri.startsWith("/h2-console")
                || uri.startsWith("/style.css") || uri.startsWith("/script.js")
                || uri.startsWith("/images/");
    }

    private void redirectLogin(HttpServletResponse response, String message) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                "<script>alert('" + message + "');window.location.href='/';</script>"
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/**") // prüft alles
                .excludePathPatterns("/", "/login", "/error", "/h2-console/**", "/style.css", "/script.js", "/images/**");
    }
}
