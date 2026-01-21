package com.example.billing.web.dto;

import java.time.LocalDate;

public class ContractUpsertDto {
    public Long client_id;
    public Long offre_id;
    public LocalDate start_date;
    public LocalDate end_date;
    public String status;
}
