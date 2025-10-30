package com.example.myapp.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CsvStorageService {

    @Value("${app.data.dir:src/main/csv_lists}")
    private String dataFolder;

public List<String[]> readCsv(String name) {
    List<String[]> result = new ArrayList<>();
    try {
        // 1. Prüfen: Externer Pfad
        Path externalPath = Path.of(dataFolder, name);
        if (Files.exists(externalPath)) {
            try (BufferedReader br = Files.newBufferedReader(externalPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    result.add(line.split(";", -1));
                }
                return result;
            }
        }

        // 2. Prüfen: classpath (falls als Ressource im JAR)
        InputStream resource = getClass().getClassLoader().getResourceAsStream("csv_lists/" + name);
        if (resource != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    result.add(line.split(";", -1));
                }
            }
        }

    } catch (IOException e) {
        throw new RuntimeException("Fehler beim Lesen von " + name, e);
    }
    return result;
}

    public void writeCsv(String name, List<String[]> data) {
        try {
            Path path = Path.of(dataFolder, name);
            Files.createDirectories(path.getParent());
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String[] row : data) {
                    bw.write(String.join(";", row));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Schreiben von " + name, e);
        }
    }
}
