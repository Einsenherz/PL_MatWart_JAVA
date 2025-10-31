package com.example.myapp.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Base64Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CsvStorageService {

    @Value("${GITHUB_TOKEN:}")
    private String githubToken;

    @Value("${GITHUB_REPO:}")
    private String githubRepo;

    @Value("${GITHUB_BRANCH:main}")
    private String githubBranch;

    private static final String LOCAL_PATH = "src/main/csv_lists/";
    private static final String API_URL = "https://api.github.com/repos/";

    // Prüft, ob lokal (z. B. in deiner IDE)
    private boolean isLocal() {
        File f = new File(LOCAL_PATH);
        return f.exists() && f.isDirectory();
    }

    /** Lies CSV */
    public List<String[]> readCsv(String filename) {
        if (isLocal()) return readLocal(filename);
        return readFromGitHub(filename);
    }

    /** Schreib CSV */
    public void writeCsv(String filename, List<String[]> data) {
        if (isLocal()) {
            writeLocal(filename, data);
        } else {
            writeToGitHub(filename, data);
        }
    }

    // -------------------------------
    // LOKAL
    // -------------------------------
    private List<String[]> readLocal(String filename) {
        List<String[]> result = new ArrayList<>();
        File file = new File(LOCAL_PATH + filename);
        if (!file.exists()) {
            System.err.println("⚠️  CSV-Datei lokal nicht gefunden: " + file.getPath());
            return result;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                if (!line.trim().isEmpty())
                    result.add(line.split(";"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void writeLocal(String filename, List<String[]> data) {
        File file = new File(LOCAL_PATH + filename);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String[] r : data) {
                bw.write(String.join(";", r));
                bw.newLine();
            }
            System.out.println("✅ Lokal gespeichert: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------
    // GITHUB
    // -------------------------------
    private List<String[]> readFromGitHub(String filename) {
        List<String[]> result = new ArrayList<>();
        try {
            String urlStr = API_URL + githubRepo + "/contents/src/main/csv_lists/" + filename + "?ref=" + githubBranch;
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestProperty("Authorization", "token " + githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3.raw");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null)
                        if (!line.trim().isEmpty())
                            result.add(line.split(";"));
                }
                System.out.println("✅ CSV aus GitHub geladen: " + filename);
            } else {
                System.err.println("⚠️  GitHub-Fehler beim Lesen " + filename + ": " + conn.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void writeToGitHub(String filename, List<String[]> data) {
        try {
            // 1. Bestehende Datei lesen, um SHA zu bekommen
            String urlStr = API_URL + githubRepo + "/contents/src/main/csv_lists/" + filename;
            HttpURLConnection getConn = (HttpURLConnection) new URL(urlStr).openConnection();
            getConn.setRequestProperty("Authorization", "token " + githubToken);
            getConn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            String sha = null;
            if (getConn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(getConn.getInputStream()))) {
                    String json = br.readLine();
                    int shaIdx = json.indexOf("\"sha\":\"");
                    if (shaIdx != -1)
                        sha = json.substring(shaIdx + 7, json.indexOf("\"", shaIdx + 7));
                }
            }

            // 2. CSV-Inhalt vorbereiten
            StringBuilder sb = new StringBuilder();
            for (String[] r : data) {
                sb.append(String.join(";", r)).append("\n");
            }
            String contentBase64 = Base64Utils.encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));

            // 3. JSON-Body erstellen
            String body = "{"
                    + "\"message\": \"Update " + filename + " via Render App\","
                    + "\"content\": \"" + contentBase64 + "\","
                    + (sha != null ? "\"sha\": \"" + sha + "\"," : "")
                    + "\"branch\": \"" + githubBranch + "\""
                    + "}";

            // 4. PUT an GitHub senden
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", "token " + githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 201 || conn.getResponseCode() == 200) {
                System.out.println("✅ CSV in GitHub gespeichert: " + filename);
            } else {
                System.err.println("⚠️  Fehler beim GitHub-Upload (" + conn.getResponseCode() + ")");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}