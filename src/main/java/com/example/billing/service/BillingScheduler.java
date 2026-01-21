package com.example.billing.service;

import com.example.billing.domain.entity.Facture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "billing.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BillingScheduler {
    private final BillingService billingService;

    @Value("${billing.scheduler.zone:UTC}")
    private String zone;

    public BillingScheduler(BillingService billingService) {
        this.billingService = billingService;
    }

    @Scheduled(cron = "${billing.scheduler.cron:0 30 2 1 * *}", zone = "${billing.scheduler.zone:UTC}")
    public void runMonthlyBilling() {
        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.minusMonths(1).withDayOfMonth(1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        List<Facture> factures = billingService.runBillingForAllClients(periodStart, periodEnd);
        // intentionally no return; scheduled task logs handled by app logging
    }
}
