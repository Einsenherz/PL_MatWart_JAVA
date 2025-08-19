package com.example.myapp.service;
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
import java.util.List;

/**
 * Zentrale Domänenlogik:
 * - Benutzerverwaltung (Admin ist geschützt)
 * - Login-Prüfung
 * - Bestellungen (inkl. Archiv-Logik)
 * - Inventar/Material
 *
 * Jede mutierende Operation triggert nach Erfolg einen Snapshot
 * via SpeicherListeSyncService.exportToGitHub(...).
 */
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

    // ====== Initialdaten ======

    /**
     * Admin-User initial anlegen, wenn nicht vorhanden.
     * Passwort wie bisher: "Dieros8500".
     * Hinweis: Der Admin wird von Listen (getAlleBenutzer) ausgenommen und ist nicht änder-/löschbar.
     */
    @PostConstruct
    public void initAdminUser() {
        if (benutzerRepository.findByUsername("admin") == null) {
            benutzerRepository.save(new Benutzer("admin", "Dieros8500"));
        }
    }

    /**
     * Kleines Startinventar beim ersten Start befüllen (wenn leer).
     */
    @PostConstruct
    public void initInventar() {
        if (materialRepository.count() == 0) {
            materialRepository.save(new Material("Hammer", 10));
            materialRepository.save(new Material("Schraubenzieher", 15));
            materialRepository.save(new Material("Bohrmaschine", 5));
        }
    }

    // ====== Benutzer ======

    /**
     * Alle Benutzer zurückgeben – aber Admin NICHT anzeigen.
     */
    public List<Benutzer> getAlleBenutzer() {
        List<Benutzer> benutzer = benutzerRepository.findAll();
        benutzer.removeIf(b -> "admin".equalsIgnoreCase(b.getUsername()));
        return benutzer;
    }

    /**
     * Benutzer hinzufügen (Admin darf nicht angelegt werden).
     */
    @Transactional
    public void addBenutzer(String username, String passwort) {
        if (!"admin".equalsIgnoreCase(username)) {
            benutzerRepository.save(new Benutzer(username, passwort));
            sync.exportToGitHub("Benutzer hinzugefügt: " + username);
        }
    }

    /**
     * Benutzer ändern (Admin ist geschützt).
     */
    @Transactional
    public void updateBenutzer(String oldUsername, String newUsername, String newPasswort) {
        if ("admin".equalsIgnoreCase(oldUsername) || "admin".equalsIgnoreCase(newUsername)) {
            return; // Admin darf nicht geändert werden
        }
        Benutzer b = benutzerRepository.findByUsername(oldUsername);
        if (b != null) {
            b.setUsername(newUsername);
            b.setPasswort(newPasswort);
            benutzerRepository.save(b);
            sync.exportToGitHub("Benutzer geändert: " + oldUsername + " -> " + newUsername);
        }
    }

    /**
     * Benutzer löschen (Admin ist geschützt).
     */
    @Transactional
    public void deleteBenutzer(String username) {
        if ("admin".equalsIgnoreCase(username)) return; // Admin darf nicht gelöscht werden
        Benutzer b = benutzerRepository.findByUsername(username);
        if (b != null) {
            benutzerRepository.delete(b);
            sync.exportToGitHub("Benutzer gelöscht: " + username);
        }
    }

    // ====== Login ======

    /**
     * Login-Check:
     * - Admin hartkodiert (admin/Dieros8500) -> "admin"
     * - sonst DB‑User -> "benutzer"
     * - ungültig -> null
     */
    public String checkLogin(String username, String passwort) {
        if ("admin".equalsIgnoreCase(username) && "Dieros8500".equals(passwort)) {
            return "admin";
        }
        Benutzer benutzer = benutzerRepository.findByUsername(username);
        if (benutzer != null && benutzer.getPasswort().equals(passwort)) {
            return "benutzer";
        }
        return null;
    }

    // ====== Bestellungen ======

    public List<Bestellung> getAlleBestellungen() {
        return bestellungRepository.findAll();
    }

    public List<Bestellung> getAlleArchiviertenBestellungenSorted() {
        return bestellungRepository.findByStatusOrderByEingabedatumDesc("Archiviert");
    }

    public List<Bestellung> getMeineBestellungen(String benutzername) {
        return bestellungRepository.findByBenutzerOrderByEingabedatumDesc(benutzername);
    }

    /**
     * Bestellung anlegen (Status = "in Bearbeitung", Eingabedatum = jetzt).
     */
    @Transactional
    public void addBestellung(String benutzer, String materialName, int anzahl) {
        Bestellung bestellung = new Bestellung();
        bestellung.setBenutzer(benutzer);
        bestellung.setMaterial(materialName);
        bestellung.setAnzahl(anzahl);
        bestellung.setStatus("in Bearbeitung");
        bestellung.setEingabedatum(LocalDateTime.now());

        bestellungRepository.save(bestellung);
        sync.exportToGitHub("Bestellung angelegt: ID " + bestellung.getId() +
                " (" + anzahl + "x " + materialName + ") für " + benutzer);
    }

    /**
     * Status ändern – ohne Bestand zu verändern.
     */
    @Transactional
    public void updateStatusOhneBestand(Long id, String status) {
        Bestellung b = bestellungRepository.findById(id).orElse(null);
        if (b != null) {
            b.setStatus(status);
            bestellungRepository.save(b);
            sync.exportToGitHub("Bestellung Status geändert (ohne Bestand): ID " + id + " -> " + status);
        }
    }

    /**
     * Status ändern – wenn "Archiviert", dann Bestand am Material reduzieren.
     */
    @Transactional
    public void updateStatusMitBestand(Long id, String status) {
        Bestellung b = bestellungRepository.findById(id).orElse(null);
        if (b != null) {
            b.setStatus(status);
            bestellungRepository.save(b);

            if ("Archiviert".equals(status)) {
                Material m = materialRepository.findByName(b.getMaterial());
                if (m != null) {
                    m.setBestand(Math.max(0, m.getBestand() - b.getAnzahl()));
                    materialRepository.save(m);
                }
            }
            sync.exportToGitHub("Bestellung Status geändert: ID " + id + " -> " + status);
        }
    }

    /**
     * Archiv komplett leeren (alle "Archiviert"-Bestellungen).
     */
    @Transactional
    public void leereArchiv() {
        List<Bestellung> archiv = getAlleArchiviertenBestellungenSorted();
        if (!archiv.isEmpty()) {
            bestellungRepository.deleteAll(archiv);
            sync.exportToGitHub("Archiv geleert (" + archiv.size() + " Einträge)");
        }
    }

    // ====== Inventar ======

    public List<Material> getAlleMaterialien() {
        return materialRepository.findAll();
    }

    /**
     * Neues Material anlegen (nur wenn Name noch nicht vorhanden).
     */
    @Transactional
    public void addMaterial(String name, int bestand) {
        if (materialRepository.findByName(name) == null) {
            materialRepository.save(new Material(name, bestand));
            sync.exportToGitHub("Material hinzugefügt: " + name + " (Bestand " + bestand + ")");
        }
    }
}
