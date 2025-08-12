package com.example.myapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Sichert /admin/** und /benutzer/** über Session-Attribut "role".
 * Lässt statische Ressourcen & Login/Logout frei.
 */
@Component
public class SecurityController implements HandlerInterceptor, WebMvcConfigurer {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String uri = request.getRequestURI();
        final HttpSession session = request.getSession(false);

        // Statische Dateien IMMER erlauben
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

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

        // Alles andere (Startseite, Login/Logout, Fehler) ist frei
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
                        "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif",
                        "/images/**", "/css/**", "/js/**", "/webjars/**",
                        "/h2-console/**"
                );
    }
}
