package com.example.billing.web.controller;

import com.example.billing.domain.entity.Offre;
import com.example.billing.domain.entity.ServiceEntity;
import com.example.billing.domain.repo.OffreRepository;
import com.example.billing.domain.repo.ServiceRepository;
import com.example.billing.service.CrmReadService;
import com.example.billing.web.dto.ContractDto;
import com.example.billing.web.dto.CustomerDto;
import com.example.billing.web.dto.OfferDto;
import com.example.billing.web.dto.OfferUpsertDto;
import com.example.billing.web.dto.ServiceDto;
import com.example.billing.web.dto.ServiceUpsertDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class BasicControllers {
    private final ServiceRepository serviceRepository;
    private final OffreRepository offreRepository;
    private final CrmReadService crmReadService;

    public BasicControllers(ServiceRepository serviceRepository,
                            OffreRepository offreRepository,
                            CrmReadService crmReadService) {
        this.serviceRepository = serviceRepository;
        this.offreRepository = offreRepository;
        this.crmReadService = crmReadService;
    }

    @GetMapping("/clientlist/")
    public List<CustomerDto> listClients() {
        return crmReadService.listCustomers();
    }

    @GetMapping("/servicelist/")
    public List<ServiceDto> listServices() {
        return serviceRepository.findAll().stream().map(this::toServiceDto).collect(Collectors.toList());
    }

    @PostMapping("/service")
    public ServiceDto createService(@RequestBody ServiceUpsertDto request) {
        ServiceEntity service = new ServiceEntity();
        applyServiceFields(service, request);
        return toServiceDto(serviceRepository.save(service));
    }

    @PutMapping("/service/{id}")
    public ServiceDto updateService(@PathVariable("id") Long id, @RequestBody ServiceUpsertDto request) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Service not found"));
        applyServiceFields(service, request);
        return toServiceDto(serviceRepository.save(service));
    }

    @DeleteMapping("/service/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable("id") Long id) {
        if (!serviceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/offrelist/")
    public List<OfferDto> listOffres() {
        return offreRepository.findAll().stream().map(this::toOfferDto).collect(Collectors.toList());
    }

    @PostMapping("/offer")
    public OfferDto createOffer(@RequestBody OfferUpsertDto request) {
        Offre offre = new Offre();
        applyOfferFields(offre, request);
        return toOfferDto(offreRepository.save(offre));
    }

    @PutMapping("/offer/{id}")
    public OfferDto updateOffer(@PathVariable("id") Long id, @RequestBody OfferUpsertDto request) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Offer not found"));
        applyOfferFields(offre, request);
        return toOfferDto(offreRepository.save(offre));
    }

    @DeleteMapping("/offer/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable("id") Long id) {
        if (!offreRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        offreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contratlist/")
    public List<ContractDto> listContrats() {
        return crmReadService.listContracts();
    }

    @GetMapping("/clientdetail/{id}/")
    public ResponseEntity<CustomerDto> clientDetail(@PathVariable("id") String id) {
        CustomerDto customer = crmReadService.getCustomer(id);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }

    @GetMapping("/servicedetail/{id}/")
    public ResponseEntity<ServiceDto> serviceDetail(@PathVariable("id") Long id) {
        return serviceRepository.findById(id)
                .map(this::toServiceDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/offredetail/{id}/")
    public ResponseEntity<OfferDto> offreDetail(@PathVariable("id") Long id) {
        return offreRepository.findById(id)
                .map(this::toOfferDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/contratdetail/{id}/")
    public ResponseEntity<ContractDto> contratDetail(@PathVariable("id") String id) {
        ContractDto contract = crmReadService.getContract(id);
        return contract != null ? ResponseEntity.ok(contract) : ResponseEntity.notFound().build();
    }

    @GetMapping("/contratlistbyClient/{clientId}/")
    public ResponseEntity<List<ContractDto>> contratsByClient(@PathVariable("clientId") String clientId) {
        return ResponseEntity.ok(crmReadService.listContractsByCustomer(clientId));
    }

    @PutMapping("/contract/{id}/deactivate")
    public ResponseEntity<ContractDto> deactivateContract(@PathVariable("id") String id) {
        crmReadService.overrideStatus(id, "inactive");
        ContractDto contract = crmReadService.getContract(id);
        return contract != null ? ResponseEntity.ok(contract) : ResponseEntity.notFound().build();
    }

    @PostMapping("/contract/{id}/cancel-request")
    public ResponseEntity<ContractDto> requestCancellation(@PathVariable("id") String id) {
        crmReadService.overrideStatus(id, "cancel_requested");
        ContractDto contract = crmReadService.getContract(id);
        return contract != null ? ResponseEntity.ok(contract) : ResponseEntity.notFound().build();
    }

    private ServiceDto toServiceDto(ServiceEntity s) {
        ServiceDto dto = new ServiceDto();
        dto.service_id = s.getServiceId();
        dto.name = s.getDesignation();
        dto.type = s.getUnite() != null ? s.getUnite().name().toLowerCase() : null;
        dto.price = s.getPrixUnite();
        dto.status = "active";
        dto.included_quantity = s.getIncludedQuantity();
        return dto;
    }

    private OfferDto toOfferDto(Offre o) {
        OfferDto dto = new OfferDto();
        dto.offer_id = o.getOffreId();
        dto.name = o.getOffreParent();
        dto.description = o.getService() != null ? "Service: " + o.getService().getDesignation() : null;
        dto.price = o.getService() != null ? o.getService().getPrixUnite() : null;
        dto.status = "active";
        return dto;
    }


    private void applyServiceFields(ServiceEntity service, ServiceUpsertDto request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Service payload is required");
        }
        service.setDesignation(request.designation);
        service.setPrixUnite(request.prix_unite != null ? request.prix_unite : BigDecimal.ZERO);
        service.setIncludedQuantity(request.included_quantity);
        if (request.unite != null && !request.unite.isBlank()) {
            try {
                service.setUnite(ServiceEntity.Unite.valueOf(request.unite.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(BAD_REQUEST, "Invalid service unit");
            }
        }
    }

    private void applyOfferFields(Offre offre, OfferUpsertDto request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Offer payload is required");
        }
        offre.setOffreParent(request.offre_parent);
        if (request.service_id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "service_id is required");
        }
        ServiceEntity service = serviceRepository.findById(request.service_id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Service not found"));
        offre.setService(service);
    }

}
