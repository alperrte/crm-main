package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tickets")
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "issue")
    private String issue;

    @Column(name = "priority")
    private String priority;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "is_employee")
    private Boolean employee = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_customer_id")
    private CustomerEntity creatorCustomer;

    @Column(name = "creator_person_id")
    private Long creatorPersonId;
}
