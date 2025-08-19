package com.example.myapp.integration;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Material;
import com.example.myapp.model.Bestellung;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.MaterialRepository;
import com.example.myapp.repository.BestellungRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SpeicherListeSyncService {

    private final GitHubStorageService gh;
    private final BenutzerRepository benutzerRepo;
    private final MaterialRepository materialRepo;
    private final BestellungRepository bestellungRepo;
    private final ObjectMapper om = new ObjectMapper();

    private final String speicherPfad;
    private String lastSha;

    public SpeicherListeSyncService(GitHubStorageService gh,
                                    BenutzerRepository benutzerRepo,
                                    MaterialRepository materialRepo,
                                    BestellungRepository bestellungRepo,
                                    Environment env) {
        this.gh = gh;
        this.benutzerRepo = benutzerRepo;
        this.materialRepo = materialRepo;
        this.bestellungRepo = bestellungRepo;
        this.speicherPfad = Objects.requireNonNullElse(
                env.getProperty("github.speicherPath"), "data/Speicherliste.json"
        );
    }

    @PostConstruct
    @Transactional
    public void importFromGitHubOnStartup() {
        try {
            GitHubStorageService.FileData fd = gh.getFile(speicherPfad);
            if (fd == null || fd.content == null || fd.content.isBlank()) {
                exportToGitHub("Initial export (no Speicherliste found)");
                return;
            }
            lastSha = fd.sha;

            SpeicherListe sl = om.readValue(fd.content, SpeicherListe.class);

            // Benutzer neu laden (Admin überspringen)
            benutzerRepo.deleteAll();
            for (Benutzer b : sl.getBenutzer()) {
                if (!"admin".equalsIgnoreCase(b.getUsername())) {
                    benutzerRepo.save(new Benutzer(b.getUsername(), b.getPasswort()));
                }
            }

            // Materialien neu laden
            materialRepo.deleteAll();
            for (Material m : sl.getMaterialien()) {
                materialRepo.save(m);
            }

            // Bestellungen neu laden
            bestellungRepo.deleteAll();
            for (Bestellung best : sl.getBestellungen()) {
                bestellungRepo.save(best);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public void exportToGitHub(String commitMessage) {
        try {
            SpeicherListe sl = new SpeicherListe();
            List<Benutzer> benutzer = benutzerRepo.findAll();
            benutzer.removeIf(b -> "admin".equalsIgnoreCase(b.getUsername())); // Admin nicht speichern
            sl.setBenutzer(benutzer);
            sl.setMaterialien(materialRepo.findAll());
            sl.setBestellungen(bestellungRepo.findAll());

            String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(sl);
            String newSha = gh.putFile(speicherPfad, json, lastSha, commitMessage);
            if (newSha != null) lastSha = newSha;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
