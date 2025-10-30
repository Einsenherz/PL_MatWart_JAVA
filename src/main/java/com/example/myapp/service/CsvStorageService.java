package com.example.myapp.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CsvStorageService {

    @Value("${app.data-folder}")
    private String dataFolder;

    public List<String[]> readCsv(String name) {
        Path path = Path.of(dataFolder, name);
        if (!Files.exists(path)) return new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            List<String[]> lines = new ArrayList<>();
            br.lines().forEach(line -> lines.add(line.split(";")));
            return lines;
        } catch (IOException e) {
            throw new RuntimeException("CSV lesen fehlgeschlagen: " + name, e);
        }
    }

    public void writeCsv(String name, List<String[]> data) {
        try {
            Files.createDirectories(Path.of(dataFolder));
            Path path = Path.of(dataFolder, name);
            try (BufferedWriter bw = Files.newBufferedWriter(path)) {
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
