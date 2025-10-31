package com.example.myapp.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvStorageService {

    private static final String BASE_PATH = "src/main/csv_lists/";

    public List<String[]> readCsv(String filename) {
        List<String[]> data = new ArrayList<>();
        File file = new File(BASE_PATH + filename);
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
        File file = new File(BASE_PATH + filename);
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