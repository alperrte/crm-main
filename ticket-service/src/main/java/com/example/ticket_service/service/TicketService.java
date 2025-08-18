// src/main/java/com/example/ticket_service/service/TicketService.java
package com.example.ticket_service.service;

import com.example.ticket_service.dto.request.TicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketRequest request);
    List<TicketResponse> getAllTickets();
}
