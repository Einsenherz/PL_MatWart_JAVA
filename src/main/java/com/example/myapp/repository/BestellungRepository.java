package com.example.myapp.repository;

import com.example.myapp.model.Bestellung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BestellungRepository extends JpaRepository<Bestellung, Long> {
    List<Bestellung> findByStatusOrderByEingabedatumDesc(String status);
    List<Bestellung> findByBenutzerOrderByEingabedatumDesc(String benutzer);
}
