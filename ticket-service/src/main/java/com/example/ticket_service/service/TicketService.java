// service/TicketService.java
package com.example.ticket_service.service;

import com.example.ticket_service.dto.request.PublicTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse createPublicTicket(PublicTicketRequest req);
    List<TicketResponse> listAllTickets();
}
