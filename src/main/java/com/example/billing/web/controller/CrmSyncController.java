package com.example.billing.web.controller;

import com.example.billing.service.CrmSyncService;
import com.example.billing.web.dto.CrmSyncResultDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm")
@CrossOrigin(origins = "http://localhost:4200")
public class CrmSyncController {
    private final CrmSyncService crmSyncService;

    public CrmSyncController(CrmSyncService crmSyncService) {
        this.crmSyncService = crmSyncService;
    }

    @PostMapping("/sync")
    public CrmSyncResultDto sync() {
        return crmSyncService.sync();
    }
}
