package com.example.myapp.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CsvStorageService {

    /**
     * Basisordner für CSV-Dateien. Konfigurierbar über application.properties:
     * app.data.dir=data
     */
    @Value("${app.data.dir:data}")
    private String dataFolder;

    /** CSV lesen: Semikolon-separiert; nicht vorhandene Datei -> leere Liste. */
    public List<String[]> readCsv(String name) {
        List<String[]> result = new ArrayList<>();
        try {
            Files.createDirectories(Path.of(dataFolder));
            Path path = Path.of(dataFolder, name);
            if (!Files.exists(path)) {
                return result; // keine Datei -> leere Liste
            }
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";", -1);
                    result.add(parts);
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("CSV lesen fehlgeschlagen: " + name, e);
        }
    }

    /** CSV schreiben: Semikolon-separiert, überschreibt die Datei. */
    public void writeCsv(String name, List<String[]> data) {
        try {
            Files.createDirectories(Path.of(dataFolder));
            Path path = Path.of(dataFolder, name);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String[] row : data) {
                    bw.write(String.join(";", row));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV schreiben fehlgeschlagen: " + name, e);
        }
    }
}
