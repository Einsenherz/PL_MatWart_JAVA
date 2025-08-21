package com.example.myapp.integration;

import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.io.IOException;

@Service
public class GitHubStorageService {
    private final Path path = Paths.get("data/speicherliste.json");

    public void save(byte[] bytes) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            // swallow in this demo (no GitHub API here)
        }
    }
}
