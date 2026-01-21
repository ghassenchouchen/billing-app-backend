package com.example.billing.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UsageDto {
    public Long usage_id;
    public Long client_id;
    public Long contract_id;
    public Long service_id;
    public BigDecimal quantity;
    public LocalDateTime event_time;
    public String source_id;
}
