package com.example.myapp.service;

import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Benutzer;
import com.example.myapp.repository.BestellungRepository;
import com.example.myapp.repository.BenutzerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListeService {

    private final BenutzerRepository benutzerRepo;
    private final BestellungRepository bestellungRepo;
    private final ZoneId zone = ZoneId.of("Europe/Berlin");  // ➔ deine Zeitzone hier festlegen

    public ListeService(BenutzerRepository benutzerRepo, BestellungRepository bestellungRepo) {
        this.benutzerRepo = benutzerRepo;
        this.bestellungRepo = bestellungRepo;
    }

    // ✅ EXISTIERENDE METHODEN HIER BELASSEN...

    // 🔥 NEUE METHODEN:

    public List<String> getAlleBenutzerNamen() {
        return benutzerRepo.findAll()
                .stream()
                .map(Benutzer::getUsername)
                .collect(Collectors.toList());
    }

    public ZoneId getZone() {
        return this.zone;
    }

    public void updateStatusMitRueckgabe(Long bestellungId, String neuerStatus) {
        bestellungRepo.findById(bestellungId).ifPresent(bestellung -> {
            bestellung.setStatus(neuerStatus);
            if ("Archiviert".equals(neuerStatus) && bestellung.getRueckgabedatum() == null) {
                bestellung.setRueckgabedatum(LocalDateTime.now(zone));
            }
            bestellungRepo.save(bestellung);
        });
    }

    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        return bestellungRepo.findAll().stream()
                .filter(b -> "Archiviert".equals(b.getStatus()))
                .sorted(Comparator
                        .comparing(Bestellung::getBenutzer)
                        .thenComparing(Bestellung::getEingabedatum))
                .collect(Collectors.toList());
    }

}
