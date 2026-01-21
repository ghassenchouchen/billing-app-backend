package com.example.billing.domain.repo;

import com.example.billing.domain.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
	Optional<ServiceEntity> findByDesignation(String designation);
}
