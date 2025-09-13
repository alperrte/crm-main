package com.example.ticket_service.controller;

import com.example.ticket_service.dto.request.PublicTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/public")
@RequiredArgsConstructor
public class PublicTicketController {
    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody PublicTicketRequest req) {
        return ResponseEntity.ok(ticketService.createPublicTicket(req));
    }
}
