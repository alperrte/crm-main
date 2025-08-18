package com.example.ticket_service.service;

import com.example.ticket_service.dto.request.TicketAssigmentRequest;
import com.example.ticket_service.dto.response.TicketAssigmentResponse;
import java.util.List;

public interface TicketAssigmentsService {
    TicketAssigmentResponse assignTicket(TicketAssigmentRequest request);
    List<TicketAssigmentResponse> getAllAssignments();
}
