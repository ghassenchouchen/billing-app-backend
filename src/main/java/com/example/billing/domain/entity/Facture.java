package com.example.billing.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facture",
    uniqueConstraints = @UniqueConstraint(name = "uk_facture_client_period",
        columnNames = {"client_id", "period_start", "period_end"}))
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long factureId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    private Integer consomAppel = 0;
    private Integer consomSms = 0;
    private Integer consomInternet = 0;

    private Boolean paid = false;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    private BigDecimal sommeTot = BigDecimal.ZERO;

    public enum InvoiceStatus {
        DRAFT,
        ISSUED,
        OVERDUE,
        PARTIALLY_PAID,
        PAID,
        CANCELLED
    }

    public Long getFactureId() { return factureId; }
    public void setFactureId(Long factureId) { this.factureId = factureId; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public List<InvoiceLine> getLines() { return lines; }
    public void setLines(List<InvoiceLine> lines) { this.lines = lines; }
    public Integer getConsomAppel() { return consomAppel; }
    public void setConsomAppel(Integer consomAppel) { this.consomAppel = consomAppel; }
    public Integer getConsomSms() { return consomSms; }
    public void setConsomSms(Integer consomSms) { this.consomSms = consomSms; }
    public Integer getConsomInternet() { return consomInternet; }
    public void setConsomInternet(Integer consomInternet) { this.consomInternet = consomInternet; }
    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public BigDecimal getSommeTot() { return sommeTot; }
    public void setSommeTot(BigDecimal sommeTot) { this.sommeTot = sommeTot; }
}
