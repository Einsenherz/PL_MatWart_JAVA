package com.example.myapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sichert automatisch Admin- und Benutzer-Routen über die Session.
 */
@Component
public class SecurityController implements HandlerInterceptor, WebMvcConfigurer {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // Admin-Bereich
        if (uri.startsWith("/admin")) {
            if (session != null && "admin".equals(session.getAttribute("role"))) {
                return true;
            } else {
                redirectLogin(response, "Bitte zuerst als Admin einloggen!");
                return false;
            }
        }

        // Benutzer-Bereich
        if (uri.startsWith("/benutzer")) {
            if (session != null && "benutzer".equals(session.getAttribute("role"))) {
                return true;
            } else {
                redirectLogin(response, "Bitte zuerst einloggen!");
                return false;
            }
        }

        // Alles andere frei (Login, Startseite, Assets)
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
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
                );
    }
}
