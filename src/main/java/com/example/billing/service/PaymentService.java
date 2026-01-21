package com.example.billing.service;

import com.example.billing.domain.entity.Facture;
import com.example.billing.domain.entity.Payment;
import com.example.billing.domain.repo.FactureRepository;
import com.example.billing.domain.repo.PaymentRepository;
import com.example.billing.web.dto.PaymentCreateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PaymentService {
    private final FactureRepository factureRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(FactureRepository factureRepository, PaymentRepository paymentRepository) {
        this.factureRepository = factureRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Facture markPaid(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Facture not found"));
        BigDecimal totalPaid = getTotalPaid(facture);
        BigDecimal remaining = facture.getSommeTot().subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            applyPaymentState(facture, totalPaid, LocalDate.now());
            return factureRepository.save(facture);
        }

        Payment payment = new Payment();
        payment.setFacture(facture);
        payment.setAmount(remaining);
        payment.setPaymentDate(LocalDate.now());
        payment.setMethod("manual");
        paymentRepository.save(payment);

        BigDecimal newTotal = totalPaid.add(remaining);
        applyPaymentState(facture, newTotal, payment.getPaymentDate());
        return factureRepository.save(facture);
    }

    @Transactional
    public Payment recordPayment(PaymentCreateDto request) {
        if (request == null || request.facture_id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "facture_id is required");
        }
        if (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "amount must be > 0");
        }
        Facture facture = factureRepository.findById(request.facture_id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Facture not found"));

        Payment payment = new Payment();
        payment.setFacture(facture);
        payment.setAmount(request.amount);
        payment.setPaymentDate(request.payment_date != null ? request.payment_date : LocalDate.now());
        payment.setMethod(request.method);
        payment.setReference(request.reference);
        payment.setNote(request.note);

        Payment saved = paymentRepository.save(payment);
        BigDecimal totalPaid = getTotalPaid(facture).add(payment.getAmount());
        applyPaymentState(facture, totalPaid, payment.getPaymentDate());
        factureRepository.save(facture);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Payment> listPayments(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Facture not found"));
        return paymentRepository.findByFacture(facture);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaid(Facture facture) {
        if (facture == null) {
            return BigDecimal.ZERO;
        }
        return paymentRepository.findByFacture(facture).stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void applyPaymentState(Facture facture, BigDecimal totalPaid, LocalDate paymentDate) {
        BigDecimal total = facture.getSommeTot() != null ? facture.getSommeTot() : BigDecimal.ZERO;
        if (totalPaid.compareTo(total) >= 0) {
            facture.setPaid(true);
            facture.setPaidDate(paymentDate != null ? paymentDate : LocalDate.now());
            facture.setStatus(Facture.InvoiceStatus.PAID);
            return;
        }
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            facture.setPaid(false);
            facture.setPaidDate(null);
            facture.setStatus(Facture.InvoiceStatus.PARTIALLY_PAID);
        } else if (facture.getStatus() == Facture.InvoiceStatus.PAID) {
            facture.setPaid(false);
            facture.setPaidDate(null);
            facture.setStatus(Facture.InvoiceStatus.ISSUED);
        }
    }
}
