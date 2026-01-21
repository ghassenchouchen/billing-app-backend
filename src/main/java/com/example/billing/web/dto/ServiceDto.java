package com.example.billing.web.dto;

import java.math.BigDecimal;

public class ServiceDto {
    public Long service_id;
    public String name;
    public String type;
    public BigDecimal price;
    public String status;
    public BigDecimal included_quantity;
}
