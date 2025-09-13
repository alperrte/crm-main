package com.example.ticket_service.repository;

import com.example.ticket_service.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    // Email adresine göre müşteri arar.
    Optional<CustomerEntity> findByEmail(String email);
}
