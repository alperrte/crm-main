package com.example.ticket_service.repository;

import com.example.ticket_service.entity.TicketAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketAssignmentRepository extends JpaRepository<TicketAssignmentEntity, Long> {
    List<TicketAssignmentEntity> findByDepartmentId(Long deptId);
    List<TicketAssignmentEntity> findByTicketId(Long ticketId);
    Optional<TicketAssignmentEntity> findByTicketIdAndDepartmentId(Long ticketId, Long deptId);
}
