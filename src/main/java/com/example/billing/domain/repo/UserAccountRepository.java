package com.example.billing.domain.repo;

import com.example.billing.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByUserNameAndPassword(String userName, String password);
}
