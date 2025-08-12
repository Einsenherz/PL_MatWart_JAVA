package com.example.myapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sichert automatisch Admin- und Benutzer-Routen.
 * Prüft die Session und lässt statische Ressourcen IMMER durch.
 */
@Component
public class SecurityController implements HandlerInterceptor, WebMvcConfigurer {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String uri = request.getRequestURI();
        final HttpSession session = request.getSession(false);

        // Admin-Bereich absichern
        if (uri.startsWith("/admin")) {
            if (session != null && "admin".equals(session.getAttribute("role"))) {
                return true;
            }
            redirectLogin(response, "Bitte zuerst als Admin einloggen!");
            return false;
        }

        // Benutzer-Bereich absichern
        if (uri.startsWith("/benutzer")) {
            if (session != null && "benutzer".equals(session.getAttribute("role"))) {
                return true;
            }
            redirectLogin(response, "Bitte zuerst einloggen!");
            return false;
        }

        // Alles andere (Startseite, Login, statische Ressourcen, h2-console, Fehlerseiten) ist frei
        return true;
    }

    private void redirectLogin(HttpServletResponse response, String message) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("<script>alert('" + message + "');window.location.href='/';</script>");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this)
        .addPathPatterns("/admin/**", "/benutzer/**")
        .excludePathPatterns(
            "/", "/login", "/logout", "/error",
            "/style.css", "/script.js",
            "/images/**", "/css/**", "/js/**", "/webjars/**",
            "/h2-console/**", "/favicon.ico"
        );
}

}

