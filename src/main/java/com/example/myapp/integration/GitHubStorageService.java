package com.example.myapp.integration;

import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Service
public class GitHubStorageService {

    private final String owner = System.getenv("GITHUB_OWNER");
    private final String repo = System.getenv("GITHUB_REPO");
    private final String branch = System.getenv("GITHUB_BRANCH") != null ? System.getenv("GITHUB_BRANCH") : "main";
    private final String token = System.getenv("GITHUB_TOKEN");
    private final String path = System.getenv("GITHUB_SPEICHERLISTE_PATH") != null ? System.getenv("GITHUB_SPEICHERLISTE_PATH") : "data/Speicherliste.json";

    /**
     * Speichert eine JSON-Datei ins GitHub Repository (erstellt Commit).
     */
    public void saveFile(String contentJson, String commitMessage) throws Exception {
        if (token == null || owner == null || repo == null) {
            throw new IllegalStateException("GitHub Environment Variablen fehlen!");
        }

        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        // JSON für GitHub API
        String payload = "{"
                + "\"message\":\"" + commitMessage + "\","
                + "\"branch\":\"" + branch + "\","
                + "\"content\":\"" + Base64.getEncoder().encodeToString(contentJson.getBytes()) + "\""
                + "}";

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "token " + token);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new RuntimeException("GitHub API Fehler: " + responseCode);
        }
    }
}
