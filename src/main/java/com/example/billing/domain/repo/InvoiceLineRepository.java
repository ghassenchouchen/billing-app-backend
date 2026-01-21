package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Facture;
import com.example.billing.domain.entity.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {
    List<InvoiceLine> findByFacture(Facture facture);
}
