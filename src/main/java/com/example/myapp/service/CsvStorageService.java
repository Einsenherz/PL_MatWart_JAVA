package com.example.myapp.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvStorageService {

    // Lokaler Pfad beim Entwickeln
    private static final String LOCAL_PATH = "src/main/csv_lists/";

    // Pfad nach dem Build bei Render (im Docker-Container)
    private static final String RENDER_PATH = "csv_lists/";

    private String getActivePath() {
        // Prüfe, ob lokale Dateien existieren
        File localDir = new File(LOCAL_PATH);
        if (localDir.exists() && localDir.isDirectory()) {
            return LOCAL_PATH;
        }
        // sonst Fallback für Render
        return RENDER_PATH;
    }

    public List<String[]> readCsv(String filename) {
        List<String[]> data = new ArrayList<>();
        String basePath = getActivePath();
        File file = new File(basePath + filename);

        if (!file.exists()) {
            System.err.println("⚠️  CSV-Datei nicht gefunden: " + file.getPath());
            return data;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    data.add(line.split(";"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void writeCsv(String filename, List<String[]> data) {
        String basePath = getActivePath();
        File file = new File(basePath + filename);

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String[] row : data) {
                bw.write(String.join(";", row));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}