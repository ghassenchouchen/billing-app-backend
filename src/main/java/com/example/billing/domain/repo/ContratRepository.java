package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Client;
import com.example.billing.domain.entity.Contrat;
import com.example.billing.domain.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByClient(Client client);

    Optional<Contrat> findByClientAndOffreAndStartDate(Client client, Offre offre, LocalDate startDate);
}
