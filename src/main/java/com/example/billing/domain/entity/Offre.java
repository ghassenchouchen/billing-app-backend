package com.example.billing.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "offre")
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offreId;

    @NotBlank
    private String offreParent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public String getOffreParent() { return offreParent; }
    public void setOffreParent(String offreParent) { this.offreParent = offreParent; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
}
