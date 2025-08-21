package com.example.myapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "material")
public class Material {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int bestand = 0;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBestand() { return bestand; }
    public void setBestand(int bestand) { this.bestand = bestand; }
}
