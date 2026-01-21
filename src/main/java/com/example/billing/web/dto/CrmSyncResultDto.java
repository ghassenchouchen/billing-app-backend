package com.example.billing.web.dto;

import java.util.ArrayList;
import java.util.List;

public class CrmSyncResultDto {
    public int customers_received;
    public int customers_upserted;
    public int contracts_received;
    public int contracts_upserted;
    public List<String> warnings = new ArrayList<>();
}
