package com.example.myapp.controller;

/**
 * Gemeinsame HTML-Helfer für alle Seiten (Header mit Logo, Footer, Breadcrumb).
 * Controller können diese Klasse einfach "extends"en und die Methoden nutzen.
 */
public abstract class BasePageController {

    protected String htmlHeader(String title) {
        return """
            <html>
              <head>
                <meta charset='UTF-8'>
                <meta name='viewport' content='width=device-width, initial-scale=1'>
                <title>""" + title + """</title>
                <link rel='stylesheet' href='/style.css'>
              </head>
              <body>
                <header>
                  <img src='/images/Logo_Pfadi_Panthera_Leo.png' alt='Logo' class='logo'>
                  <h1>""" + title + """</h1>
                </header>
                <main class='centered-content'>
            """;
    }

    protected String htmlFooter() {
        return """
                </main>
              </body>
            </html>
            """;
    }

    protected String breadcrumb(String homePath, String path) {
        return "<div class='breadcrumb'><a href='" + homePath + "'>Home</a> &gt; " + path + "</div>";
    }
}
