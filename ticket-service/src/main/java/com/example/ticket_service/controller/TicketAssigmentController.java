package com.example.ticket_service.controller;

import com.example.ticket_service.dto.request.TicketAssigmentRequest;
import com.example.ticket_service.dto.response.TicketAssigmentResponse;
import com.example.ticket_service.service.TicketAssigmentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class TicketAssigmentController {

    private final TicketAssigmentsService ticketAssigmentsService;

    @PostMapping
    public ResponseEntity<TicketAssigmentResponse> assign(@RequestBody TicketAssigmentRequest request) {
        TicketAssigmentResponse resp = ticketAssigmentsService.assignTicket(request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<TicketAssigmentResponse>> getAll() {
        return ResponseEntity.ok(ticketAssigmentsService.getAllAssignments());
    }
}
