package com.example.billing.service;

import com.example.billing.web.dto.CrmSyncResultDto;
import org.springframework.stereotype.Service;

@Service
public class CrmSyncService {
    private final CrmReadService crmReadService;

    public CrmSyncService(CrmReadService crmReadService) {
        this.crmReadService = crmReadService;
    }

    public CrmSyncResultDto sync() {
        CrmSyncResultDto result = new CrmSyncResultDto();
        result.customers_received = crmReadService.listCustomers().size();
        result.contracts_received = crmReadService.listContracts().size();
        result.customers_upserted = 0;
        result.contracts_upserted = 0;
        result.warnings.add("CRM sync is read-only; no local persistence performed.");
        return result;
    }
}
