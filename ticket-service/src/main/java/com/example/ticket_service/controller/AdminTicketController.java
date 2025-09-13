package com.example.ticket_service.controller;

import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> all() {
        return ResponseEntity.ok(ticketService.listAllTickets());
    }
}
