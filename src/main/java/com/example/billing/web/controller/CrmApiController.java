package com.example.billing.web.controller;

import com.example.billing.domain.entity.Client;
import com.example.billing.domain.entity.Contrat;
import com.example.billing.domain.repo.ClientRepository;
import com.example.billing.domain.repo.ContratRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crm")
@CrossOrigin(origins = "http://localhost:4200")
public class CrmApiController {
    private final ClientRepository clientRepository;
    private final ContratRepository contratRepository;

    public CrmApiController(ClientRepository clientRepository, ContratRepository contratRepository) {
        this.clientRepository = clientRepository;
        this.contratRepository = contratRepository;
    }

    @GetMapping("/customers")
    public List<CustomerCrmDto> getCustomers() {
        return clientRepository.findAll().stream()
                .map(this::toCustomerCrmDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/contracts")
    public List<ContractCrmDto> getContracts() {
        return contratRepository.findAll().stream()
                .map(this::toContractCrmDto)
                .collect(Collectors.toList());
    }

    private CustomerCrmDto toCustomerCrmDto(Client client) {
        CustomerCrmDto dto = new CustomerCrmDto();
        dto.email = client.getEmail();
        dto.nom = client.getNom();
        dto.prenom = client.getPrenom();
        dto.adresse = client.getAdresse();
        dto.type = client.getType() != null ? client.getType().toString() : "SIMPLE";
        return dto;
    }

    private ContractCrmDto toContractCrmDto(Contrat contrat) {
        ContractCrmDto dto = new ContractCrmDto();
        dto.customer_email = contrat.getClient() != null ? contrat.getClient().getEmail() : null;
        dto.offer_name = contrat.getOffre() != null ? contrat.getOffre().getOffreParent() : null;
        dto.start_date = contrat.getStartDate() != null ? contrat.getStartDate().toString() : null;
        dto.end_date = contrat.getEndDate() != null ? contrat.getEndDate().toString() : null;
        dto.status = "active";
        return dto;
    }

    public static class CustomerCrmDto {
        public String email;
        public String nom;
        public String prenom;
        public String adresse;
        public String type;
    }

    public static class ContractCrmDto {
        public String customer_email;
        public String offer_name;
        public String start_date;
        public String end_date;
        public String status;
    }
}
