package com.example.myapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class SpeicherListeSyncService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GitHubStorageService gh;

    public SpeicherListeSyncService(GitHubStorageService gh) {
        this.gh = gh;
    }

    public void sync(SpeicherListe liste) {
        try {
            byte[] bytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(liste);
            gh.save(bytes);
        } catch (Exception ignored) {}
    }
}
