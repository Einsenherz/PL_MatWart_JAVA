package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class BenutzerService {
    private static final String DATA_DIR = "data";
    private static final String CSV_FILE = DATA_DIR + "/benutzer.csv";

    private final List<Benutzer> benutzerListe = new ArrayList<>();

    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "admin123";

    public BenutzerService() {
        initialisiereDatei();
        ladeBenutzer();
    }

    private void initialisiereDatei() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Path path = Paths.get(CSV_FILE);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ladeBenutzer() {
        benutzerListe.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    benutzerListe.add(new Benutzer(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void speichereBenutzer() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (Benutzer b : benutzerListe) {
                bw.write(b.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean istAdmin(String username, String passwort) {
        return ADMIN_USER.equals(username) && ADMIN_PASS.equals(passwort);
    }

    public boolean existiertBenutzer(String username, String passwort) {
        return benutzerListe.stream()
                .anyMatch(b -> b.getUsername().equals(username) && b.getPasswort().equals(passwort));
    }

    public void hinzufuegen(Benutzer benutzer) {
        if (!benutzer.getUsername().equalsIgnoreCase(ADMIN_USER)) {
            benutzerListe.add(benutzer);
            speichereBenutzer();
        }
    }

    public void loeschen(String username) {
        benutzerListe.removeIf(b -> b.getUsername().equalsIgnoreCase(username));
        speichereBenutzer();
    }

    public List<Benutzer> getAlle() {
        return benutzerListe;
    }
}
