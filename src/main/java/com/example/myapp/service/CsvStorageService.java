package com.example.myapp.service;

import com.example.myapp.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Service für das Laden und Speichern von CSV-Dateien.
 * Unterstützt Lesen aus dem Projekt (src/main/csv_lists) und aus dem Classpath im JAR.
 */
@Service
public class CsvStorageService {

    private static final String RESOURCE_FOLDER = "csv_lists";

    /**
     * CSV lesen: versucht zuerst im Projektverzeichnis, dann im Classpath.
     * Trenner: Semikolon (;)
     */
    public List<String[]> readCsv(String filename) {
        List<String[]> result = new ArrayList<>();

        try {
            // 1.Versuch: externe Datei (z. B. beim Entwickeln)
            Path path = Path.of("src/main/csv_lists", filename);
            if (Files.exists(path)) {
                try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.add(line.split(";", -1));
                    }
                    return result;
                }
            }

            // 2.Versuch: im Classpath (z. B. im JAR bei Render)
            InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "/" + filename);
            if (in != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.add(line.split(";", -1));
                    }
                    return result;
                }
            }

            System.err.println("⚠️  CSV-Datei nicht gefunden: " + filename);
            return result;

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der CSV-Datei: " + filename, e);
        }
    }

    /**
     * CSV schreiben: überschreibt die Datei unter src/main/csv_lists.
     */
    public void writeCsv(String filename, List<String[]> data) {
        try {
            Path folder = Path.of("src/main/csv_lists");
            Files.createDirectories(folder);
            Path path = folder.resolve(filename);

            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String[] row : data) {
                    bw.write(String.join(";", row));
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Schreiben der CSV-Datei: " + filename, e);
        }
    }
}
