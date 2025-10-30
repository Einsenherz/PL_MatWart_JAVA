package com.example.myapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

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
            + "<title>" + escape(title) + "</title>"
            + "<link rel='stylesheet' href='/style.css'>"
            + "</head>"
            + "<body>"
            + "<header>"
            + "<img src='/images/Logo_Pfadi_Panthera_Leo.png' class='logo' alt='Pfadi Panthera Leo Logo'>"
            + "<h1>" + escape(title) + "</h1>"
            + "</header>"
            + "<main>";
    }

    protected String htmlFooter() {
        return "</main></body></html>";
    }

    /** Breadcrumb-Zeile oben auf der Seite. */
    protected String breadcrumb(String rootTitle, String current) {
        return new StringBuilder()
                .append("<nav class='breadcrumb'>")
                .append("<a href='/home'>").append(escape(rootTitle)).append("</a>")
                .append(" &raquo; ")
                .append("<span>").append(escape(current)).append("</span>")
                .append("</nav>")
                .toString();
    }

    /** HTML-Escaping (z. B. für Tabellenausgaben). */
    protected String escape(String text) {
        return HtmlUtils.htmlEscape(text == null ? "" : text);
    }
}
