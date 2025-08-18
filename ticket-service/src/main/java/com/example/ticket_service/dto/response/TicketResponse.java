package com.example.ticket_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String issue;
    private String priority;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime closedDate;

    private Boolean employee;
    private Long creatorCustomerId;
    private Long creatorPersonId;
}
