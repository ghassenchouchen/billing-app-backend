package com.example.billing.web.controller;

import com.example.billing.domain.entity.Facture;
import com.example.billing.domain.entity.InvoiceLine;
import com.example.billing.domain.entity.Client;
import com.example.billing.domain.repo.FactureRepository;
import com.example.billing.domain.repo.InvoiceLineRepository;
import com.example.billing.domain.repo.ClientRepository;
import com.example.billing.service.BillingService;
import com.example.billing.service.PaymentService;
import com.example.billing.web.dto.BillDto;
import com.example.billing.web.dto.InvoiceLineDto;
import com.example.billing.web.dto.BillingRunDto;
import com.example.billing.web.dto.CleanupResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class BillController {
    private final FactureRepository factureRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final BillingService billingService;
    private final PaymentService paymentService;
    private final ClientRepository clientRepository;

    public BillController(FactureRepository factureRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          BillingService billingService,
                          PaymentService paymentService,
                          ClientRepository clientRepository) {
        this.factureRepository = factureRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.billingService = billingService;
        this.paymentService = paymentService;
        this.clientRepository = clientRepository;
    }

    @GetMapping("/facturelist/")
    public List<BillDto> listBills() {
        return factureRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/facturedetail/{id}/")
    public ResponseEntity<BillDto> billDetail(@PathVariable("id") Long id) {
        return factureRepository.findById(id)
                .map(f -> ResponseEntity.ok(toDto(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/facturelistbyClient/{clientId}/")
    public ResponseEntity<List<BillDto>> billsByClient(@PathVariable("clientId") Long clientId) {
        return clientRepository.findById(clientId)
                .map(c -> factureRepository.findByClient(c).stream().map(this::toDto).collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/facturelistbyCustomer/{customerId}/")
    public ResponseEntity<List<BillDto>> billsByCustomer(@PathVariable("customerId") String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return clientRepository.findByEmail(customerId)
                .map(c -> factureRepository.findByClient(c).stream().map(this::toDto).collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(List.of()));
    }

    @PutMapping("/sommefacturebyfactureID/{id}/")
    public ResponseEntity<BillDto> calculate(@PathVariable("id") Long id) {
        Facture updated = billingService.calculateTotalForBill(id);
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/billing/run")
    public ResponseEntity<List<BillDto>> runBilling(@RequestBody BillingRunDto request) {
        if (request == null || request.period_start == null || request.period_end == null) {
            throw new ResponseStatusException(BAD_REQUEST, "period_start and period_end are required");
        }
        List<Facture> factures = billingService.runBillingForAllClients(request.period_start, request.period_end);
        List<BillDto> result = factures.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/billing/cleanup")
    public ResponseEntity<CleanupResultDto> cleanupFactures() {
        return ResponseEntity.ok(billingService.cleanupFactures());
    }

    @GetMapping("/facturelines/{factureId}/")
    public ResponseEntity<List<InvoiceLineDto>> linesByFacture(@PathVariable("factureId") Long factureId) {
        return factureRepository.findById(factureId)
                .map(f -> invoiceLineRepository.findByFacture(f).stream().map(this::toLineDto).collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private BillDto toDto(Facture f) {
        BillDto dto = new BillDto();
        dto.facture_id = f.getFactureId();
        dto.client_id = f.getClient().getClientId();
        dto.client_name = joinName(f.getClient().getPrenom(), f.getClient().getNom());
        dto.consom_appel = f.getConsomAppel();
        dto.consom_sms = f.getConsomSms();
        dto.consom_internet = f.getConsomInternet();
        dto.paid = Boolean.TRUE.equals(f.getPaid());
        dto.somme_tot = f.getSommeTot();
        dto.total_paid = paymentService.getTotalPaid(f);
        if (dto.somme_tot != null && dto.total_paid != null) {
            dto.balance_due = dto.somme_tot.subtract(dto.total_paid);
        }
        dto.period_start = f.getPeriodStart();
        dto.period_end = f.getPeriodEnd();
        dto.issue_date = f.getIssueDate();
        dto.due_date = f.getDueDate();
        dto.paid_date = f.getPaidDate();
        dto.status = f.getStatus() != null ? f.getStatus().name().toLowerCase() : null;
        dto.date = f.getIssueDate();
        return dto;
    }

    private InvoiceLineDto toLineDto(InvoiceLine line) {
        InvoiceLineDto dto = new InvoiceLineDto();
        dto.line_id = line.getLineId();
        dto.facture_id = line.getFacture() != null ? line.getFacture().getFactureId() : null;
        dto.service_id = line.getService() != null ? line.getService().getServiceId() : null;
        dto.service_name = line.getService() != null ? line.getService().getDesignation() : null;
        dto.quantity = line.getQuantity();
        dto.unit_price = line.getUnitPrice();
        dto.amount = line.getAmount();
        dto.description = line.getDescription();
        return dto;
    }

    private String joinName(String first, String last) {
        String full = String.join(" ", java.util.Arrays.asList(
                first != null ? first.trim() : "",
                last != null ? last.trim() : ""
        )).trim();
        return full.isBlank() ? null : full;
    }
}
