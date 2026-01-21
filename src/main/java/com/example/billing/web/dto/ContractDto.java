package com.example.billing.web.dto;

import java.time.LocalDate;

public class ContractDto {
    public String contract_id;
    public String customer_id;
    public String customer_name;
    public String type;
    public LocalDate start_date;
    public LocalDate end_date;
    public String status;
}
