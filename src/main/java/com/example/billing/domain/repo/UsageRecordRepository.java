package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Client;
import com.example.billing.domain.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {
    List<UsageRecord> findByClient(Client client);
    List<UsageRecord> findByClientAndEventTimeBetween(Client client, LocalDateTime start, LocalDateTime end);
    List<UsageRecord> findByClientAndEventTimeAfter(Client client, LocalDateTime start);
    List<UsageRecord> findByClientAndEventTimeBefore(Client client, LocalDateTime end);
    Optional<UsageRecord> findBySourceId(String sourceId);
}
