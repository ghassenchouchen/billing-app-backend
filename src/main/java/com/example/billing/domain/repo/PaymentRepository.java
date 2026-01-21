package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Facture;
import com.example.billing.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByFacture(Facture facture);
}
