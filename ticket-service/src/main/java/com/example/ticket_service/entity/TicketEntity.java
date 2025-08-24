// entity/TicketEntity.java
package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "issue", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String issue;

    @Column(name = "priority", nullable = false, length = 50)
    private String priority; // LOW / MEDIUM / HIGH (string)

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "is_employee", nullable = false)
    private Boolean employee; // public için false

    // Creator: Customer (public başvuru)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_customer_id")
    private CustomerEntity creatorCustomer;

    // Person creator yok; şema gereği opsiyonel:
    @Column(name = "creator_person_id")
    private Long creatorPersonId;
}
