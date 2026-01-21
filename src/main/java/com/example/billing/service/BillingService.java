package com.example.billing.service;

import com.example.billing.domain.entity.*;
import com.example.billing.domain.repo.ContratRepository;
import com.example.billing.domain.repo.FactureRepository;
import com.example.billing.domain.repo.InvoiceLineRepository;
import com.example.billing.domain.repo.PaymentRepository;
import com.example.billing.domain.repo.UsageRecordRepository;
import com.example.billing.domain.repo.ClientRepository;
import com.example.billing.web.dto.CleanupResultDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Comparator;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BillingService {
    private final FactureRepository factureRepository;
    private final ContratRepository contratRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;

    public BillingService(FactureRepository factureRepository,
                          ContratRepository contratRepository,
                          UsageRecordRepository usageRecordRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          PaymentRepository paymentRepository,
                          ClientRepository clientRepository) {
        this.factureRepository = factureRepository;
        this.contratRepository = contratRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Facture calculateTotalForBill(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Facture not found"));

        Client client = facture.getClient();
        if (client == null) {
            throw new ResponseStatusException(NOT_FOUND, "Client not found for facture");
        }
        List<Contrat> contrats = contratRepository.findByClient(client);

        LocalDate periodStart = facture.getPeriodStart();
        LocalDate periodEnd = facture.getPeriodEnd();

        List<UsageRecord> usageRecords = findUsageForPeriod(client, periodStart, periodEnd);

        Map<ServiceEntity, BigDecimal> quantityByService = new HashMap<>();
        for (UsageRecord record : usageRecords) {
            if (record.getService() == null) {
                continue;
            }
            Contrat contrat = record.getContrat();
            if (contrat != null) {
                if (contrat.getStatus() != null && contrat.getStatus() != Contrat.ContractStatus.ACTIVE) {
                    continue;
                }
                if (!isContractActiveForPeriod(contrat, periodStart, periodEnd)) {
                    continue;
                }
            }
            BigDecimal quantity = record.getQuantity() == null ? BigDecimal.ZERO : record.getQuantity();
            quantityByService.merge(record.getService(), quantity, BigDecimal::add);
        }

        facture.getLines().clear();
        BigDecimal totalSms = BigDecimal.ZERO;
        BigDecimal totalSeconds = BigDecimal.ZERO;
        BigDecimal totalOctets = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<ServiceEntity, BigDecimal> entry : quantityByService.entrySet()) {
            ServiceEntity service = entry.getKey();
            BigDecimal quantity = entry.getValue();
            BigDecimal unitPrice = service.getPrixUnite() == null ? BigDecimal.ZERO : service.getPrixUnite();
            BigDecimal amount = unitPrice.multiply(quantity);

            ServiceEntity.Unite unit = resolveUnit(service);
            if (unit == ServiceEntity.Unite.SMS) {
                totalSms = totalSms.add(quantity);
            } else if (unit == ServiceEntity.Unite.SECONDE) {
                totalSeconds = totalSeconds.add(quantity);
            } else if (unit == ServiceEntity.Unite.OCTET) {
                totalOctets = totalOctets.add(quantity);
            }

            InvoiceLine line = new InvoiceLine();
            line.setFacture(facture);
            line.setService(service);
            line.setQuantity(quantity);
            line.setUnitPrice(unitPrice);
            line.setAmount(amount);
            line.setDescription(service.getDesignation());
            facture.getLines().add(line);

            total = total.add(amount);
        }
        facture.setSommeTot(total);
        facture.setConsomSms(totalSms.intValue());
        facture.setConsomAppel(totalSeconds.intValue());
        facture.setConsomInternet(totalOctets.intValue());
        if (facture.getIssueDate() == null) {
            facture.setIssueDate(LocalDate.now());
        }
        if (facture.getStatus() == null || facture.getStatus() == Facture.InvoiceStatus.DRAFT) {
            facture.setStatus(Facture.InvoiceStatus.ISSUED);
        }
        return factureRepository.save(facture);
    }

    @Transactional
    public Facture generateOrUpdateInvoiceForPeriod(Client client, LocalDate periodStart, LocalDate periodEnd) {
        if (client == null) {
            throw new ResponseStatusException(NOT_FOUND, "Client not found");
        }
        Facture facture = factureRepository.findByClientAndPeriodStartAndPeriodEnd(client, periodStart, periodEnd);
        if (facture == null) {
            facture = new Facture();
            facture.setClient(client);
            facture.setPeriodStart(periodStart);
            facture.setPeriodEnd(periodEnd);
            facture.setIssueDate(LocalDate.now());
            if (periodEnd != null) {
                facture.setDueDate(periodEnd.plusDays(15));
            }
            facture.setStatus(Facture.InvoiceStatus.DRAFT);
        }
        Facture saved = factureRepository.save(facture);
        return calculateTotalForBill(saved.getFactureId());
    }

    @Transactional
    public List<Facture> runBillingForAllClients(LocalDate periodStart, LocalDate periodEnd) {
        return clientRepository.findAll().stream()
                .map(c -> generateOrUpdateInvoiceForPeriod(c, periodStart, periodEnd))
                .toList();
    }

    private List<UsageRecord> findUsageForPeriod(Client client, LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart != null && periodEnd != null) {
            LocalDateTime start = periodStart.atStartOfDay();
            LocalDateTime end = periodEnd.plusDays(1).atStartOfDay().minusNanos(1);
            return usageRecordRepository.findByClientAndEventTimeBetween(client, start, end);
        }
        if (periodStart != null) {
            LocalDateTime start = periodStart.atStartOfDay();
            return usageRecordRepository.findByClientAndEventTimeAfter(client, start);
        }
        if (periodEnd != null) {
            LocalDateTime end = periodEnd.plusDays(1).atStartOfDay().minusNanos(1);
            return usageRecordRepository.findByClientAndEventTimeBefore(client, end);
        }
        return usageRecordRepository.findByClient(client);
    }

    private boolean isContractActiveForPeriod(Contrat contrat, LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null && periodEnd == null) {
            return true;
        }
        LocalDate contractStart = contrat.getStartDate();
        LocalDate contractEnd = contrat.getEndDate();
        LocalDate effectiveStart = periodStart != null ? periodStart : LocalDate.MIN;
        LocalDate effectiveEnd = periodEnd != null ? periodEnd : LocalDate.MAX;
        if (contractStart != null && contractStart.isAfter(effectiveEnd)) {
            return false;
        }
        if (contractEnd != null && contractEnd.isBefore(effectiveStart)) {
            return false;
        }
        return true;
    }

    private ServiceEntity.Unite resolveUnit(ServiceEntity service) {
        if (service == null) {
            return ServiceEntity.Unite.OCTET;
        }
        if (service.getUnite() != null) {
            return service.getUnite();
        }
        String name = service.getDesignation() != null ? service.getDesignation().toLowerCase() : "";
        if (name.contains("sms")) {
            return ServiceEntity.Unite.SMS;
        }
        if (name.contains("appel") || name.contains("call") || name.contains("voice")) {
            return ServiceEntity.Unite.SECONDE;
        }
        return ServiceEntity.Unite.OCTET;
    }

    @Transactional
    public CleanupResultDto cleanupFactures() {
        List<Facture> all = factureRepository.findAll();
        Map<String, List<Facture>> grouped = new HashMap<>();
        List<Facture> dirty = new ArrayList<>();

        for (Facture f : all) {
            if (f.getClient() == null || f.getPeriodStart() == null || f.getPeriodEnd() == null) {
                dirty.add(f);
                continue;
            }
            String key = f.getClient().getClientId() + "|" + f.getPeriodStart() + "|" + f.getPeriodEnd();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(f);
        }

        List<Facture> duplicates = new ArrayList<>();
        Comparator<Facture> keepOrder = Comparator
                .comparing(Facture::getIssueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Facture::getFactureId, Comparator.nullsLast(Comparator.naturalOrder()));

        for (List<Facture> list : grouped.values()) {
            if (list.size() <= 1) {
                continue;
            }
            list.sort(keepOrder);
            Facture keep = list.get(list.size() - 1);
            for (Facture f : list) {
                if (!Objects.equals(f.getFactureId(), keep.getFactureId())) {
                    duplicates.add(f);
                }
            }
        }

        int dirtyDeleted = deleteFacturesWithPayments(dirty);
        int duplicatesDeleted = deleteFacturesWithPayments(duplicates);

        CleanupResultDto result = new CleanupResultDto();
        result.dirty_deleted = dirtyDeleted;
        result.duplicates_deleted = duplicatesDeleted;
        result.total_deleted = dirtyDeleted + duplicatesDeleted;
        return result;
    }

    private int deleteFacturesWithPayments(List<Facture> factures) {
        int deleted = 0;
        for (Facture facture : factures) {
            if (facture.getFactureId() == null) {
                continue;
            }
            paymentRepository.deleteAll(paymentRepository.findByFacture(facture));
            factureRepository.delete(facture);
            deleted++;
        }
        return deleted;
    }
}
