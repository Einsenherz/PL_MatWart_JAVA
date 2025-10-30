package com.example.myapp.controller;

import org.springframework.stereotype.Controller;

/**
 * Basiscontroller für alle Seiten mit HTML-Header und Footer.
 * Wird von allen anderen Controllern geerbt.
 */
@Controller
public abstract class BasePageController {

    protected String htmlHeader(String title) {
        return "<!DOCTYPE html>"
            + "<html>"
            + "<head>"
            + "<meta charset='UTF-8'>"
            + "<title>" + title + "</title>"
            + "<link rel='stylesheet' href='/style.css'>"
            + "</head>"
            + "<body>"
            + "<header>"
            + "<img src='/images/Logo_Pfadi_Panthera_Leo.png' class='logo' alt='Pfadi Panthera Leo Logo'>"
            + "<h1>" + title + "</h1>"
            + "</header>"
            + "<main>";
    }

    protected String htmlFooter() {
        return "</main></body></html>";
    }
}
