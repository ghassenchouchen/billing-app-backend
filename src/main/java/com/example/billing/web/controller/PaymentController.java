package com.example.billing.web.controller;

import com.example.billing.domain.entity.Payment;
import com.example.billing.service.PaymentService;
import com.example.billing.web.dto.PaymentCreateDto;
import com.example.billing.web.dto.PaymentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentDto> createPayment(@RequestBody PaymentCreateDto request) {
        Payment payment = paymentService.recordPayment(request);
        return ResponseEntity.ok(toDto(payment));
    }

    @GetMapping("/payments/byfacture/{factureId}/")
    public ResponseEntity<List<PaymentDto>> listByFacture(@PathVariable("factureId") Long factureId) {
        List<PaymentDto> result = paymentService.listPayments(factureId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.payment_id = payment.getPaymentId();
        dto.facture_id = payment.getFacture() != null ? payment.getFacture().getFactureId() : null;
        dto.amount = payment.getAmount();
        dto.payment_date = payment.getPaymentDate();
        dto.method = payment.getMethod();
        dto.reference = payment.getReference();
        dto.note = payment.getNote();
        return dto;
    }
}
