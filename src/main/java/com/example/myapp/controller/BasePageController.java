package com.example.myapp.controller;

/**
 * Gemeinsame HTML-Helfer für alle Seiten (Header mit Logo, Footer, Breadcrumb).
 * Funktioniert ohne Java-Textblöcke → kompatibel mit Java 8+.
 */
public abstract class BasePageController {

    protected String htmlHeader(String title) {
        return "<html>\n"
                + "<head>\n"
                + "  <meta charset='UTF-8'>\n"
                + "  <meta name='viewport' content='width=device-width, initial-scale=1'>\n"
                + "  <title>" + title + "</title>\n"
                + "  <link rel='stylesheet' href='/style.css'>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <header>\n"
                + "    <img src='/images/Logo_Pfadi_Panthera_Leo.png' alt='Logo' class='logo'>\n"
                + "    <h1>" + title + "</h1>\n"
                + "  </header>\n"
                + "  <main class='centered-content'>\n";
    }

    protected String htmlFooter() {
        return "  </main>\n"
                + "</body>\n"
                + "</html>\n";
    }

    protected String breadcrumb(String homePath, String path) {
        return "<div class='breadcrumb'><a href='" + homePath + "'>Home</a> &gt; " + path + "</div>";
    }
}
