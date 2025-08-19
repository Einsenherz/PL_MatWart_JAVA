package com.example.myapp.integration;

import com.example.myapp.model.Benutzer;
import com.example.myapp.model.Bestellung;
import com.example.myapp.model.Material;
import com.example.myapp.repository.BenutzerRepository;
import com.example.myapp.repository.BestellungRepository;
import com.example.myapp.repository.MaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpeicherListeSyncService {

    private final BenutzerRepository benutzerRepository;
    private final BestellungRepository bestellungRepository;
    private final MaterialRepository materialRepository;
    private final GitHubStorageService gitHubStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpeicherListeSyncService(BenutzerRepository benutzerRepository,
                                    BestellungRepository bestellungRepository,
                                    MaterialRepository materialRepository,
                                    GitHubStorageService gitHubStorageService) {
        this.benutzerRepository = benutzerRepository;
        this.bestellungRepository = bestellungRepository;
        this.materialRepository = materialRepository;
        this.gitHubStorageService = gitHubStorageService;
    }

    /**
     * Exportiert ALLES in eine JSON-Datei und pusht nach GitHub
     */
    public void exportToGitHub(String commitMessage) {
        try {
            Map<String, Object> snapshot = new HashMap<>();
            List<Benutzer> benutzer = benutzerRepository.findAll();
            benutzer.removeIf(b -> "admin".equalsIgnoreCase(b.getUsername())); // Admin nicht speichern

            snapshot.put("benutzer", benutzer);
            snapshot.put("bestellungen", bestellungRepository.findAll());
            snapshot.put("materialien", materialRepository.findAll());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);
            gitHubStorageService.saveFile(json, commitMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
