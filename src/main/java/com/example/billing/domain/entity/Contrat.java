package com.example.billing.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contrat")
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contratId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offre_id")
    private Offre offre;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.ACTIVE;

    public enum ContractStatus {
        ACTIVE,
        SUSPENDED,
        TERMINATED
    }

    public Long getContratId() { return contratId; }
    public void setContratId(Long contratId) { this.contratId = contratId; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Offre getOffre() { return offre; }
    public void setOffre(Offre offre) { this.offre = offre; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
}
