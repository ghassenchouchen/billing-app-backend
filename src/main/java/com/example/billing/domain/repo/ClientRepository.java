package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
	Optional<Client> findByEmail(String email);
}
