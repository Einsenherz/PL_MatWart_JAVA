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
 */
@Component
public class SecurityController implements HandlerInterceptor, WebMvcConfigurer {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // --- Admin-Bereich absichern ---
        if (uri.startsWith("/admin")) {
            if (session != null && "admin".equals(session.getAttribute("role"))) {
                return true;
            } else {
                redirectLogin(response, "Bitte zuerst als Admin einloggen!");
                return false;
            }
        }

        // --- Benutzer-Bereich absichern ---
        if (uri.startsWith("/benutzer")) {
            if (session != null && "benutzer".equals(session.getAttribute("role"))) {
                return true;
            } else {
                redirectLogin(response, "Bitte zuerst einloggen!");
                return false;
            }
        }

        // Andere Routen (Startseite, Login etc.) sind frei
        return true;
    }

    /**
     * Hilfsmethode für einen Redirect mit Alert-Meldung.
     */
    private void redirectLogin(HttpServletResponse response, String message) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                "<script>alert('" + message + "');window.location.href='/';</script>"
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/admin/**", "/benutzer/**")
                .excludePathPatterns(
                        "/", "/login", "/error",
                        "/css/**", "/js/**", "/images/**", "/style.css"
                );
    }
}
