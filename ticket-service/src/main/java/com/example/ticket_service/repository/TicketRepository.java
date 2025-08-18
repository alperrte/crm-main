// src/main/java/com/example/ticket_service/repository/TicketRepository.java
package com.example.ticket_service.repository;

import com.example.ticket_service.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
}
