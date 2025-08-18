package com.example.ticket_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketAssigmentResponse {

    private Long id;
    private Long ticketId;
    private Boolean pool;
    private Long departmentId;
    private Long personId;
    private LocalDateTime assigned;
    private LocalDateTime completed;
    private String status;
}
