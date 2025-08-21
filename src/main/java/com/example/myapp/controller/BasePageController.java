package com.example.myapp.controller;

public class BasePageController {
    protected String htmlHeader(String title) {
        return "<!DOCTYPE html><html lang='de'><head><meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
               "<title>"+escape(title)+"</title>" +
               "<link rel='stylesheet' href='/style.css'></head><body>" +
               "<header><h1>"+escape(title)+"</h1></header><main>";
    }
    protected String htmlFooter() {
        return "</main><script src='/script.js'></script></body></html>";
    }
    protected String breadcrumb(String href, String label) {
        return "<div class='notice'><strong>Pfad:</strong> <a href='"+href+"'>"+escape(label)+"</a></div>";
    }
    protected String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
