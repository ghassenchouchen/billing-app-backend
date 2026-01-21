package com.example.billing.web.dto;

import java.math.BigDecimal;

public class InvoiceLineDto {
    public Long line_id;
    public Long facture_id;
    public Long service_id;
    public String service_name;
    public BigDecimal quantity;
    public BigDecimal unit_price;
    public BigDecimal amount;
    public String description;
}
