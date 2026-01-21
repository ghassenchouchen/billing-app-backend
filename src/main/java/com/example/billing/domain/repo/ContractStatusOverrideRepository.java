package com.example.billing.domain.repo;

import com.example.billing.domain.entity.ContractStatusOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractStatusOverrideRepository extends JpaRepository<ContractStatusOverride, Long> {
    Optional<ContractStatusOverride> findByContractId(String contractId);
}
