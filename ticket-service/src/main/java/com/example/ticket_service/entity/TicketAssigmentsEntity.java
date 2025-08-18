// src/main/java/com/example/ticket_service/entity/TicketAssigmentsEntity.java
package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ticket_assignments")
public class TicketAssigmentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "is_in_pool", nullable = false)
    private Boolean inPool = false;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "assigned_date")
    private LocalDateTime assigned;

    @Column(name = "completed_date")
    private LocalDateTime completed;

    @Column(name = "status", nullable = false)
    private String status = "OPEN";
}
