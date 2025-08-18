package com.example.ticket_service.repository;

import com.example.ticket_service.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {

    /** email benzersiz; kayıt öncesi kontrol & login senaryoları için */
    Optional<CustomerEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
