package com.example.ticket_service.service;

import com.example.ticket_service.dto.request.InternalTicketRequest;
import com.example.ticket_service.dto.request.PublicTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse createPublicTicket(PublicTicketRequest req);
    List<TicketResponse> listAllTickets();

    // ✅ Yeni eklenenler
    TicketResponse takeTicket(Long ticketId, Long deptId);
    TicketResponse reassignTicket(Long ticketId, Long fromDeptId, Long toDeptId);
    TicketResponse closeTicket(Long ticketId);
    List<TicketResponse> listTicketsByDepartment(Long deptId);
    TicketResponse createInternalTicket(InternalTicketRequest req, Long deptId);
}
