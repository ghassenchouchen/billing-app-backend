package com.example.billing.web.dto;

import java.time.LocalDate;
import java.util.List;

public class UsageGenerateDto {
    public Long client_id;
    public Long contract_id;
    public List<Long> service_ids;
    public LocalDate start_date;
    public LocalDate end_date;
    public Integer events_per_service;
}
