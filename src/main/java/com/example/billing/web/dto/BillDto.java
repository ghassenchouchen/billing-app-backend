package com.example.billing.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillDto {
    public Long facture_id;
    public Long client_id;
    public String client_name;
    public Integer consom_appel;
    public Integer consom_sms;
    public Integer consom_internet;
    public Boolean paid;
    public BigDecimal somme_tot;
    public BigDecimal total_paid;
    public BigDecimal balance_due;
    public LocalDate period_start;
    public LocalDate period_end;
    public LocalDate issue_date;
    public LocalDate due_date;
    public LocalDate paid_date;
    public String status;
    public LocalDate date;
}