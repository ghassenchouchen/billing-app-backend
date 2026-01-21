package com.example.billing.service;

import com.example.billing.domain.entity.ContractStatusOverride;
import com.example.billing.domain.repo.ContractStatusOverrideRepository;
import com.example.billing.web.dto.ContractDto;
import com.example.billing.web.dto.CustomerDto;
import com.example.billing.web.dto.MockoonContractDto;
import com.example.billing.web.dto.MockoonCustomerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrmReadService {
    private final ContractStatusOverrideRepository statusOverrideRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${crm.mockoon.base-url:http://localhost:3001}")
    private String mockoonBaseUrl;

    @Value("${crm.mockoon.customers-path:/customers}")
    private String mockoonCustomersPath;

    @Value("${crm.mockoon.contracts-path:/contracts}")
    private String mockoonContractsPath;


    public CrmReadService(ContractStatusOverrideRepository statusOverrideRepository) {
        this.statusOverrideRepository = statusOverrideRepository;
    }

    public List<CustomerDto> listCustomers() {
        return listMockoonCustomers();
    }

    public CustomerDto getCustomer(String customerId) {
        if (customerId == null) {
            return null;
        }
        return listCustomers().stream()
                .filter(c -> customerId.equalsIgnoreCase(c.client_id))
                .findFirst()
                .orElse(null);
    }

    public List<ContractDto> listContracts() {
        return listMockoonContracts();
    }

    public ContractDto getContract(String contractId) {
        if (contractId == null) {
            return null;
        }
        return listContracts().stream()
                .filter(c -> contractId.equalsIgnoreCase(c.contract_id))
                .findFirst()
                .orElse(null);
    }

    public List<ContractDto> listContractsByCustomer(String customerId) {
        if (customerId == null) {
            return List.of();
        }
        return listContracts().stream()
                .filter(c -> c.customer_id != null && c.customer_id.equalsIgnoreCase(customerId))
                .collect(Collectors.toList());
    }

    public void overrideStatus(String contractId, String status) {
        if (contractId == null || status == null) {
            return;
        }
        ContractStatusOverride override = statusOverrideRepository.findByContractId(contractId)
                .orElseGet(ContractStatusOverride::new);
        override.setContractId(contractId);
        override.setStatus(status.toLowerCase());
        statusOverrideRepository.save(override);
    }

    private List<CustomerDto> listMockoonCustomers() {
        String customersUrl = mockoonBaseUrl + mockoonCustomersPath;
        MockoonCustomerDto[] customers = restTemplate.getForObject(customersUrl, MockoonCustomerDto[].class);
        if (customers == null) {
            return List.of();
        }
        List<CustomerDto> result = new ArrayList<>();
        for (MockoonCustomerDto customer : customers) {
            if (customer == null || customer.email == null || customer.email.isBlank()) {
                continue;
            }
            CustomerDto dto = new CustomerDto();
            dto.client_id = customer.email;
            dto.nom = customer.nom;
            dto.prenom = customer.prenom;
            dto.adresse = customer.adresse;
            dto.email = customer.email;
            dto.type = normalizeType(customer.type);
            result.add(dto);
        }
        return result;
    }

    // TMF support removed

    private List<ContractDto> listMockoonContracts() {
        String contractsUrl = mockoonBaseUrl + mockoonContractsPath;
        MockoonContractDto[] contracts = restTemplate.getForObject(contractsUrl, MockoonContractDto[].class);
        Map<String, CustomerDto> customersByEmail = listMockoonCustomers().stream()
                .filter(c -> c.email != null)
                .collect(Collectors.toMap(c -> c.email, c -> c, (a, b) -> a));
        if (contracts == null) {
            return List.of();
        }
        List<ContractDto> result = new ArrayList<>();
        for (MockoonContractDto contract : contracts) {
            if (contract == null || contract.customer_email == null || contract.customer_email.isBlank()) {
                continue;
            }
            ContractDto dto = new ContractDto();
            dto.customer_id = contract.customer_email;
            dto.contract_id = buildContractId(contract.customer_email, contract.offer_name, contract.start_date);
            CustomerDto customer = customersByEmail.get(contract.customer_email);
            if (customer != null) {
                dto.customer_name = joinName(customer.prenom, customer.nom);
            }
            dto.type = contract.offer_name;
            dto.start_date = contract.start_date;
            dto.end_date = contract.end_date;
            dto.status = normalizeStatus(contract.status);
            applyOverride(dto);
            result.add(dto);
        }
        return result;
    }

    // TMF support removed

    private void applyOverride(ContractDto dto) {
        if (dto == null || dto.contract_id == null) {
            return;
        }
        statusOverrideRepository.findByContractId(dto.contract_id)
                .map(ContractStatusOverride::getStatus)
                .filter(s -> s != null && !s.isBlank())
                .ifPresent(status -> dto.status = status.toLowerCase());
    }

    // TMF helpers removed

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return type.trim().toLowerCase();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "active";
        }
        return status.trim().toLowerCase();
    }

    private String joinName(String first, String last) {
        String full = String.join(" ", Arrays.asList(
                first != null ? first.trim() : "",
                last != null ? last.trim() : ""
        )).trim();
        return full.isBlank() ? null : full;
    }

    private String buildContractId(String email, String offerName, LocalDate startDate) {
        String key = String.join("|",
                email != null ? email : "unknown",
                offerName != null ? offerName : "offer",
                startDate != null ? startDate.toString() : "start");
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes());
    }
}
