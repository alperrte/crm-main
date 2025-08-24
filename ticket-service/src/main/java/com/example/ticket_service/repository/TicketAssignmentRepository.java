// repository/TicketAssignmentRepository.java
package com.example.ticket_service.repository;

import com.example.ticket_service.entity.TicketAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAssignmentRepository extends JpaRepository<TicketAssignmentEntity, Long> {
}
