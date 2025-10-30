package com.example.myapp.service;

import com.example.myapp.model.Benutzer;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthService {

    public Benutzer findByUsername(List<Benutzer> users, String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public boolean checkPassword(Benutzer user, String password) {
        return user != null && user.getPassword().equals(password);
    }
}
