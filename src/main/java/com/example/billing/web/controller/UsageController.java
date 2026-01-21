package com.example.billing.web.controller;

import com.example.billing.domain.entity.Client;
import com.example.billing.domain.entity.Contrat;
import com.example.billing.domain.entity.ServiceEntity;
import com.example.billing.domain.entity.UsageRecord;
import com.example.billing.domain.repo.ClientRepository;
import com.example.billing.domain.repo.ContratRepository;
import com.example.billing.domain.repo.ServiceRepository;
import com.example.billing.domain.repo.UsageRecordRepository;
import com.example.billing.web.dto.UsageDto;
import com.example.billing.web.dto.UsageIngestDto;
import com.example.billing.web.dto.UsageGenerateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class UsageController {
    private final UsageRecordRepository usageRecordRepository;
    private final ClientRepository clientRepository;
    private final ContratRepository contratRepository;
    private final ServiceRepository serviceRepository;

    public UsageController(UsageRecordRepository usageRecordRepository,
                           ClientRepository clientRepository,
                           ContratRepository contratRepository,
                           ServiceRepository serviceRepository) {
        this.usageRecordRepository = usageRecordRepository;
        this.clientRepository = clientRepository;
        this.contratRepository = contratRepository;
        this.serviceRepository = serviceRepository;
    }

    @PostMapping("/usage/ingest")
    public ResponseEntity<List<UsageDto>> ingest(@RequestBody List<UsageIngestDto> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Payload is empty");
        }

        List<UsageRecord> existing = new ArrayList<>();
        List<UsageRecord> toCreate = new ArrayList<>();

        for (UsageIngestDto dto : payload) {
            if (dto.source_id != null && !dto.source_id.isBlank()) {
                usageRecordRepository.findBySourceId(dto.source_id)
                        .ifPresentOrElse(existing::add, () -> toCreate.add(toEntity(dto)));
            } else {
                toCreate.add(toEntity(dto));
            }
        }

        List<UsageRecord> saved = toCreate.isEmpty() ? new ArrayList<>() : usageRecordRepository.saveAll(toCreate);
        List<UsageDto> result = Stream.concat(existing.stream(), saved.stream())
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/usagelist/")
    public List<UsageDto> listUsage() {
        return usageRecordRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/usagelistbyClient/{clientId}/")
    public ResponseEntity<List<UsageDto>> listUsageByClient(@PathVariable("clientId") Long clientId) {
        return clientRepository.findById(clientId)
                .map(c -> usageRecordRepository.findByClient(c).stream().map(this::toDto).collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/usage/generate")
    public ResponseEntity<List<UsageDto>> generateDemoUsage(@RequestBody UsageGenerateDto request) {
        if (request == null || request.client_id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "client_id is required");
        }
        Client client = clientRepository.findById(request.client_id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Client not found"));

        Contrat contrat = null;
        if (request.contract_id != null) {
            contrat = contratRepository.findById(request.contract_id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Contract not found"));
        }

        List<ServiceEntity> services;
        if (request.service_ids != null && !request.service_ids.isEmpty()) {
            services = request.service_ids.stream()
                    .map(id -> serviceRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Service not found")))
                    .collect(Collectors.toList());
        } else {
            services = serviceRepository.findAll();
        }

        if (services.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No services available to generate usage");
        }

        LocalDate start = request.start_date != null ? request.start_date : LocalDate.now().withDayOfMonth(1);
        LocalDate end = request.end_date != null ? request.end_date : LocalDate.now();
        if (end.isBefore(start)) {
            throw new ResponseStatusException(BAD_REQUEST, "end_date must be after start_date");
        }

        int eventsPerService = request.events_per_service != null ? request.events_per_service : 5;
        Random random = new Random();
        List<UsageRecord> records = new ArrayList<>();

        for (ServiceEntity service : services) {
            for (int i = 0; i < eventsPerService; i++) {
                UsageRecord record = new UsageRecord();
                record.setClient(client);
                record.setContrat(contrat);
                record.setService(service);
                record.setQuantity(generateQuantity(service, random));
                record.setEventTime(randomDateTime(start, end, random));
                record.setSourceId("demo-" + UUID.randomUUID());
                records.add(record);
            }
        }

        List<UsageRecord> saved = usageRecordRepository.saveAll(records);
        return ResponseEntity.ok(saved.stream().map(this::toDto).collect(Collectors.toList()));
    }

    private UsageRecord toEntity(UsageIngestDto dto) {
        if (dto.client_id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "client_id is required");
        }
        if (dto.quantity == null) {
            throw new ResponseStatusException(BAD_REQUEST, "quantity is required");
        }

        Client client = clientRepository.findById(dto.client_id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Client not found"));

        Contrat contrat = null;
        if (dto.contract_id != null) {
            contrat = contratRepository.findById(dto.contract_id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Contract not found"));
        }

        ServiceEntity service = null;
        if (dto.service_id != null) {
            service = serviceRepository.findById(dto.service_id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Service not found"));
        }

        UsageRecord record = new UsageRecord();
        record.setClient(client);
        record.setContrat(contrat);
        record.setService(service);
        record.setQuantity(dto.quantity);
        record.setEventTime(dto.event_time != null ? dto.event_time : LocalDateTime.now());
        record.setSourceId(dto.source_id != null ? dto.source_id : UUID.randomUUID().toString());
        return record;
    }

    private UsageDto toDto(UsageRecord r) {
        UsageDto dto = new UsageDto();
        dto.usage_id = r.getUsageId();
        dto.client_id = r.getClient() != null ? r.getClient().getClientId() : null;
        dto.contract_id = r.getContrat() != null ? r.getContrat().getContratId() : null;
        dto.service_id = r.getService() != null ? r.getService().getServiceId() : null;
        dto.quantity = r.getQuantity();
        dto.event_time = r.getEventTime();
        dto.source_id = r.getSourceId();
        return dto;
    }

    private LocalDateTime randomDateTime(LocalDate start, LocalDate end, Random random) {
        long startEpoch = start.toEpochDay();
        long endEpoch = end.toEpochDay();
        long randomDay = startEpoch + random.nextInt((int) (endEpoch - startEpoch + 1));
        LocalDate date = LocalDate.ofEpochDay(randomDay);
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        return LocalDateTime.of(date, LocalTime.of(hour, minute, second));
    }

    private java.math.BigDecimal generateQuantity(ServiceEntity service, Random random) {
        if (service.getUnite() == ServiceEntity.Unite.SMS) {
            return java.math.BigDecimal.valueOf(1 + random.nextInt(5));
        }
        if (service.getUnite() == ServiceEntity.Unite.SECONDE) {
            return java.math.BigDecimal.valueOf(30 + random.nextInt(300));
        }
        return java.math.BigDecimal.valueOf(1024 + random.nextInt(50000));
    }
}
