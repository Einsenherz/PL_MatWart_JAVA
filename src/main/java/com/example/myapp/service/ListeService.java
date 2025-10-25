package com.example.myapp.service;

import com.example.myapp.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ListeService {
    private static final String BENUTZER_FILE = "src/main/resources/data/benutzer.csv";
    private static final String MATERIAL_FILE = "src/main/resources/data/materialliste.csv";
    private static final String BESTELL_FILE = "src/main/resources/data/bestellungen.csv";

    // ---------- MATERIALIEN ----------
    public List<Material> ladeMaterialien() {
        List<Material> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(MATERIAL_FILE))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                list.add(new Material(Long.parseLong(p[0]), p[1], Integer.parseInt(p[2])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void speichereMaterialien(List<Material> materialien) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(MATERIAL_FILE))) {
            bw.write("id,name,bestand\n");
            for (Material m : materialien) {
                bw.write(m.getId() + "," + m.getName() + "," + m.getBestand() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- BENUTZER ----------
    public List<Benutzer> ladeBenutzer() {
        List<Benutzer> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(BENUTZER_FILE))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                list.add(new Benutzer(p[0], p[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---------- BESTELLUNGEN ----------
    public List<Bestellung> ladeBestellungen() {
        List<Bestellung> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(BESTELL_FILE))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                list.add(new Bestellung(
                        Long.parseLong(p[0]),
                        p[1], p[2],
                        Integer.parseInt(p[3]),
                        LocalDateTime.parse(p[4]),
                        LocalDateTime.parse(p[5])
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void speichereBestellungen(List<Bestellung> bestellungen) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(BESTELL_FILE))) {
            bw.write("id,benutzer,material,anzahl,eingabedatum,rueckgabedatum\n");
            for (Bestellung b : bestellungen) {
                bw.write(b.getId() + "," + b.getBenutzer() + "," + b.getMaterial() + "," + b.getAnzahl() + "," +
                        b.getEingabedatum() + "," + b.getRueckgabedatum() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

