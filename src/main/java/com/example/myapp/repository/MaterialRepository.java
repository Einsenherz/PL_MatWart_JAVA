package com.example.myapp.repository;

import com.example.myapp.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByName(String name);
    Material findByName(String name);
}
