package com.example.billing.domain.repo;

import com.example.billing.domain.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long> {
    List<Offre> findByOffreParent(String offreParent);
}
