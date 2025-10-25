package com.example.myapp.service;

import com.example.myapp.model.Material;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class MaterialService {
    private static final String DATA_DIR = "data";
    private static final String CSV_FILE = DATA_DIR + "/materialliste.csv";
    private final List<Material> materialien = new ArrayList<>();

    public MaterialService() {
        initialisiereDatei();
        ladeMaterialien();
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

    public void ladeMaterialien() {
        materialien.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    materialien.add(new Material(parts[0], Integer.parseInt(parts[1]), parts[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void speichere() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (Material m : materialien) {
                bw.write(m.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Material> getAlle() { return materialien; }

    public void hinzufuegen(Material m) {
        materialien.add(m);
        speichere();
    }

    public void loeschen(String name) {
        materialien.removeIf(m -> m.getName().equalsIgnoreCase(name));
        speichere();
    }

    public void bearbeiten(String altName, Material neu) {
        for (int i = 0; i < materialien.size(); i++) {
            if (materialien.get(i).getName().equalsIgnoreCase(altName)) {
                materialien.set(i, neu);
                break;
            }
        }
        speichere();
    }
}
