package com.example.myapp.service;

import com.example.myapp.model.Bestellung;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class BestellungService {
    private static final String DATA_DIR = "data";
    private static final String CSV_FILE = DATA_DIR + "/bestellungen.csv";
    private final List<Bestellung> bestellungen = new ArrayList<>();

    public BestellungService() {
        initialisiereDatei();
        ladeBestellungen();
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

    public void ladeBestellungen() {
        bestellungen.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    bestellungen.add(new Bestellung(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void speichere() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (Bestellung b : bestellungen) {
                bw.write(b.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Bestellung> getAlle() {
        return bestellungen;
    }

    public List<Bestellung> getVonBenutzer(String benutzername) {
        List<Bestellung> eigene = new ArrayList<>();
        for (Bestellung b : bestellungen) {
            if (b.getBenutzername().equalsIgnoreCase(benutzername)) {
                eigene.add(b);
            }
        }
        return eigene;
    }

    public void hinzufuegen(Bestellung bestellung) {
        bestellungen.add(bestellung);
        speichere();
    }

    public void loeschen(int index) {
        if (index >= 0 && index < bestellungen.size()) {
            bestellungen.remove(index);
            speichere();
        }
    }

    public void statusAendern(int index, String neuerStatus) {
        if (index >= 0 && index < bestellungen.size()) {
            bestellungen.get(index).setStatus(neuerStatus);
            speichere();
        }
    }
}
