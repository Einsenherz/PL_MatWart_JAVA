package com.example.myapp.controller;

public class BasePageController {

    public static String htmlHeader(String title) {
        return "<html><head><meta charset='UTF-8'><title>" + title + "</title>"
             + "<link rel='stylesheet' href='/style.css'>"
             + "</head><body>"
             + "<header>"
             + "  <img src='/images/Logo_Pfadi_Panthera_Leo.png' alt='Logo' class='logo'>"
             + "  <h1>" + title + "</h1>"
             + "</header>"
             + "<main class='centered-content'>";
    }

    public static String htmlFooter() {
        return "</main></body></html>";
    }
}
