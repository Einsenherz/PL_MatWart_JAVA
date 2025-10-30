package com.example.myapp.controller;

import org.springframework.ui.Model;

public abstract class BasePageController {

    protected String htmlHeader(String title) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title>"
                + "<link rel='stylesheet' href='/style.css'></head><body><header><h1>" + title + "</h1></header><main>";
    }

    protected String htmlFooter() {
        return "</main><footer><p>&copy; 2025 MatWart</p></footer></body></html>";
    }

    protected String breadcrumb(String... parts) {
        StringBuilder sb = new StringBuilder("<nav class='breadcrumb'>");
        for (String part : parts) sb.append("<span>").append(part).append("</span> / ");
        sb.append("</nav>");
        return sb.toString();
    }

    protected String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    protected void addMessage(Model model, String message) {
        model.addAttribute("message", message);
    }
}
