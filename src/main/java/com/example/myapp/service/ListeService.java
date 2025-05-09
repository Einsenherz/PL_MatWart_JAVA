package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ListeService {
    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;
    private final ZoneId zone = ZoneId.of("Europe/Zurich");

    public ListeService(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    public boolean checkPasswort(String username, String passwort) {
        return benutzerRepo.findById(username)
                .map(b -> b.getPasswort().equals(passwort))
                .orElse(false);
    }

    public void addBenutzer(String username, String passwort) {
        benutzerRepo.save(new Benutzer(username, passwort));
    }

    public List<Benutzer> getAlleBenutzer() {
        return benutzerRepo.findAll();
    }

    public List<Bestellung> getBestellungen(String benutzer) {
        return bestellungRepo.findByBenutzer(benutzer)
                .stream()
                .filter(b -> !"Archiviert".equals(b.getStatus()))
                .toList();
    }

    public List<Bestellung> getAlleArchiviertenBestellungen() {
        return bestellungRepo.findAll()
                .stream()
                .filter(b -> "Archiviert".equals(b.getStatus()))
                .sorted(Comparator
                        .comparing(Bestellung::getEingabedatum, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bestellung::getMaterial, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public void saveBestellung(Bestellung b) {
        bestellungRepo.save(b);
    }

    public Optional<Bestellung> findBestellungById(Long id) {
        return bestellungRepo.findById(id);
    }

    public void bestelle(String benutzer, int anzahl, String material) {
        Bestellung b = new Bestellung(benutzer, anzahl, material, "in Bearbeitung");
        bestellungRepo.save(b);
    }

    public void updateStatusMitRueckgabe(Long id, String status) {
        bestellungRepo.findById(id).ifPresent(b -> {
            b.setStatus(status);
            if ("Archiviert".equals(status) && b.getRueckgabedatum() == null) {
                b.setRueckgabedatum(LocalDateTime.now(zone));
            }
            bestellungRepo.save(b);
        });
    }

    public void markiereAlsAbgegeben(String benutzer) {
        List<Bestellung> liste = bestellungRepo.findByBenutzer(benutzer);
        for (Bestellung b : liste) {
            if (b.getEingabedatum() == null) {
                b.setEingabedatum(LocalDateTime.now(zone));
                bestellungRepo.save(b);
            }
        }
    }

    public boolean loescheBestellungWennMoeglich(Long id) {
        Optional<Bestellung> opt = bestellungRepo.findById(id);
        if (opt.isPresent()) {
            Bestellung b = opt.get();
            if ("in Bearbeitung".equals(b.getStatus()) && b.getEingabedatum() == null) {
                bestellungRepo.deleteById(id);
                return true;
            }
        }
        return false;
    }
}
