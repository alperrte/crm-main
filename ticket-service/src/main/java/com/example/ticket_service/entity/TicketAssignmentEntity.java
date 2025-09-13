package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_assignments")
public class TicketAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private TicketEntity ticket;

    @Column(name = "is_in_pool")
    private Boolean inPool;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "status")
    private String status;
}
