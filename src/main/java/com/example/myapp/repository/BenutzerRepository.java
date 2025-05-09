package com.example.myapp.repository;

import com.example.myapp.model.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenutzerRepository extends JpaRepository<Benutzer, String> {
    // Username als Primärschlüssel (String)
}
