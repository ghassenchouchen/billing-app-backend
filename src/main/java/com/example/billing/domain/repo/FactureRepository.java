package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Client;
import com.example.billing.domain.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {
	List<Facture> findByClient(Client client);
	Facture findByClientAndPeriodStartAndPeriodEnd(Client client, java.time.LocalDate periodStart, java.time.LocalDate periodEnd);
}
