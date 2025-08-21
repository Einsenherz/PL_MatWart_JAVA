package com.example.myapp.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.Base64;

@Service
public class GitHubStorageService {

    @Value("${github.enabled:false}")
    private boolean enabled;

    @Value("${github.token:}")
    private String token;

    @Value("${github.repoOwner:}")
    private String repoOwner;

    @Value("${github.repoName:}")
    private String repoName;

    @Value("${github.branch:main}")
    private String branch;

    @Value("${github.filePath:data/speicherliste.json}")
    private String filePath;

    @Value("${github.commitMessage:Sync: speicherliste.json aktualisiert}")
    private String commitMessage;

    private final Path localPath = Paths.get("data/speicherliste.json");
    private final HttpClient http = HttpClient.newHttpClient();

    public void save(byte[] bytes) {
        // 1) Immer lokal schreiben (Fallback + Debug)
        try {
            Files.createDirectories(localPath.getParent());
            Files.write(localPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {}

        // 2) Optional: Auf GitHub committen
        if (!enabled || token == null || token.isBlank() ||
            repoOwner.isBlank() || repoName.isBlank() || filePath.isBlank()) {
            return; // kein GitHub-Sync konfiguriert
        }
        try {
            String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                repoOwner, repoName, filePath
            );

            // Zuerst SHA der aktuellen Datei holen (PUT braucht sie zum Update; bei neuem File lassen wir sie weg)
            String existingSha = null;
            var getReq = HttpRequest.newBuilder(URI.create(apiUrl + "?ref=" + branch))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .GET().build();
            var getResp = http.send(getReq, HttpResponse.BodyHandlers.ofString());
            if (getResp.statusCode() == 200) {
                // ganz simpel, ohne JSON-Parser: SHA grob extrahieren
                String body = getResp.body();
                int i = body.indexOf("\"sha\":\"");
                if (i >= 0) {
                    int j = body.indexOf('"', i + 7);
                    if (j > i) existingSha = body.substring(i + 7, j);
                }
            }

            // Payload bauen
            String contentB64 = Base64.getEncoder().encodeToString(bytes);
            String safeMsg = commitMessage + " (" + Instant.now() + ")";
            String json = "{"
                    + "\"message\":\"" + escapeJson(safeMsg) + "\","
                    + "\"content\":\"" + contentB64 + "\","
                    + "\"branch\":\"" + escapeJson(branch) + "\""
                    + (existingSha != null ? ",\"sha\":\"" + existingSha + "\"" : "")
                    + "}";

            var putReq = HttpRequest.newBuilder(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            http.send(putReq, HttpResponse.BodyHandlers.ofString());
            // Wir ignorieren bewusst die Rückgabe – Fehler hier sollen nicht den Hauptfluss stören
        } catch (Exception ignored) {
            // keine harte Fehlersanktion – App läuft weiter, Sync ist best-effort
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

