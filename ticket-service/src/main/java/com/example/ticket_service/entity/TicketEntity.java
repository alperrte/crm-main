// src/main/java/com/example/ticket_service/entity/TicketEntity.java
package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "issue", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String issue;

    @Column(name = "priority", nullable = false, length = 50)
    private String priority;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "is_employee", nullable = false)
    private Boolean employee = false;

    @Column(name = "creator_customer_id")
    private Long creatorCustomerId;

    @Column(name = "creator_person_id")
    private Long creatorPersonId;
}
