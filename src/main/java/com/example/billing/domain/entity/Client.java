package com.example.billing.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clientId;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    private String adresse;

    @Email
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private ClientType type;

    public enum ClientType { BUSINESS, SIMPLE }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public ClientType getType() { return type; }
    public void setType(ClientType type) { this.type = type; }
}
