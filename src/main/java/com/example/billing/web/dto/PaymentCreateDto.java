package com.example.billing.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentCreateDto {
    public Long facture_id;
    public BigDecimal amount;
    public LocalDate payment_date;
    public String method;
    public String reference;
    public String note;
}
