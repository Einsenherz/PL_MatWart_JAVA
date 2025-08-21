package com.example.myapp.service;

import com.example.myapp.integration.SpeicherListe;
import com.example.myapp.integration.SpeicherListeSyncService;
import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import com.example.myapp.repository.MaterialRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ListeService {

    private final BenutzerRepository benutzerRepository;
    private final BestellungRepository bestellungRepository;
    private final MaterialRepository materialRepository;
    private final SpeicherListeSyncService sync;

    public ListeService(BenutzerRepository benutzerRepository,
                        BestellungRepository bestellungRepository,
                        MaterialRepository materialRepository,
                        SpeicherListeSyncService sync) {
        this.benutzerRepository = benutzerRepository;
        this.bestellungRepository = bestellungRepository;
        this.materialRepository = materialRepository;
        this.sync = sync;
    }

    @PostConstruct
    public void initAdminUser() {
        if (!benutzerRepository.existsByUsername("admin")) {
            Benutzer admin = new Benutzer();
            admin.setUsername("admin");
            admin.setPasswort("admin");
            admin.setRolle("ADMIN");
            benutzerRepository.save(admin);
        }
    }

    // ---- Benutzer ----
    public List<Benutzer> getAlleBenutzer() { return benutzerRepository.findAll(); }

    @Transactional
    public void addBenutzer(String username, String passwort) {
        if (benutzerRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username bereits vorhanden");
        }
        Benutzer b = new Benutzer();
        b.setUsername(username);
        b.setPasswort(passwort);
        b.setRolle("USER");
        benutzerRepository.save(b);
        syncAll();
    }

    @Transactional
    public void updateBenutzer(String oldUsername, String newUsername, String newPasswort) {
        Benutzer b = benutzerRepository.findByUsername(oldUsername);
        if (b == null) throw new IllegalArgumentException("Benutzer nicht gefunden: " + oldUsername);
        if (!oldUsername.equals(newUsername) && benutzerRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Neuer Username bereits vergeben");
        }
        b.setUsername(newUsername);
        b.setPasswort(newPasswort);
        benutzerRepository.save(b);
        syncAll();
    }

    @Transactional
    public void deleteBenutzer(String username) {
        benutzerRepository.deleteByUsername(username);
        syncAll();
    }

    public boolean validateLogin(String username, String passwort) {
        Benutzer b = benutzerRepository.findByUsername(username);
        return b != null && b.getPasswort().equals(passwort);
    }

    // ---- Bestellungen ----
    public List<Bestellung> getAlleBestellungen() {
        return bestellungRepository.findAll();
    }

    @Transactional
    public void updateStatusMitBestand(Long bestellungId, String status) {
        Bestellung b = bestellungRepository.findById(bestellungId)
                .orElseThrow(() -> new IllegalArgumentException("Bestellung nicht gefunden"));
        String prev = b.getStatus();
        b.setStatus(status);
        if ("Archiviert".equals(status) && !"Archiviert".equals(prev)) {
            b.setRueckgabedatum(LocalDateTime.now());
            // Bestand verringern: Material match nach Name
            if (b.getMaterial() != null) {
                Material m = materialRepository.findByName(b.getMaterial());
                if (m != null) {
                    m.setBestand(Math.max(0, m.getBestand() - Math.max(0, b.getAnzahl())));
                    materialRepository.save(m);
                }
            }
        }
        bestellungRepository.save(b);
        syncAll();
    }

    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        List<Bestellung> list = bestellungRepository.findByStatus("Archiviert");
        list.sort(Comparator.comparing(Bestellung::getRueckgabedatum,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return list;
    }

    @Transactional
    public void leereArchiv() {
        for (Bestellung b : bestellungRepository.findByStatus("Archiviert")) {
            bestellungRepository.delete(b);
        }
        syncAll();
    }

    // ---- Material ----
    public List<Material> getAlleMaterialien() { return materialRepository.findAll(); }

    @Transactional
    public Material createMaterial(String name, int bestand) {
        if (materialRepository.existsByName(name)) {
            throw new IllegalArgumentException("Material bereits vorhanden: " + name);
        }
        Material m = new Material();
        m.setName(name);
        m.setBestand(Math.max(0, bestand));
        materialRepository.save(m);
        syncAll();
        return m;
    }

    @Transactional
    public Material updateMaterial(Long id, String name, int bestand) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material nicht gefunden"));
        if (!m.getName().equals(name) && materialRepository.existsByName(name)) {
            throw new IllegalArgumentException("Name bereits vorhanden");
        }
        m.setName(name);
        m.setBestand(Math.max(0, bestand));
        materialRepository.save(m);
        syncAll();
        return m;
    }

    @Transactional
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
        syncAll();
    }

    private void syncAll() {
        try {
            SpeicherListe liste = new SpeicherListe();
            liste.benutzer = benutzerRepository.findAll();
            liste.bestellungen = bestellungRepository.findAll();
            liste.material = materialRepository.findAll();
            sync.sync(liste);
        } catch (Exception ignored) {}
    }
}
