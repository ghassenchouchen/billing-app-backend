package com.example.billing.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @NotBlank
    private String designation; // SMS, Appel, Internet

    @NotNull
    private BigDecimal prixUnite = BigDecimal.ZERO;

    private BigDecimal includedQuantity; // Quantity included (messages, MB, minutes)

    @Enumerated(EnumType.STRING)
    private Unite unite; // SECONDE, OCTET, SMS

    public enum Unite { SECONDE, OCTET, SMS }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public BigDecimal getPrixUnite() { return prixUnite; }
    public void setPrixUnite(BigDecimal prixUnite) { this.prixUnite = prixUnite; }
    public Unite getUnite() { return unite; }
    public void setUnite(Unite unite) { this.unite = unite; }
    public BigDecimal getIncludedQuantity() { return includedQuantity; }
    public void setIncludedQuantity(BigDecimal includedQuantity) { this.includedQuantity = includedQuantity; }
}
