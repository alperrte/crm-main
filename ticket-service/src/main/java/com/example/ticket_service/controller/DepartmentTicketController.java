package com.example.ticket_service.controller;

import com.example.ticket_service.dto.request.InternalTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentTicketController {

    private final TicketService ticketService;

    // Departmanın tüm ticketları
    @GetMapping("/{deptId}/tickets")
    public ResponseEntity<List<TicketResponse>> ticketsByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(ticketService.listTicketsByDepartment(deptId));
    }

    // Ticket üstlenme
    @PutMapping("/{ticketId}/take")
    public ResponseEntity<TicketResponse> take(@PathVariable Long ticketId,
                                               @RequestParam Long deptId) {
        return ResponseEntity.ok(ticketService.takeTicket(ticketId, deptId));
    }

    // Ticket devretme
    @PutMapping("/{ticketId}/reassign/{newDeptId}")
    public ResponseEntity<TicketResponse> reassign(@PathVariable Long ticketId,
                                                   @RequestParam Long fromDeptId,
                                                   @PathVariable Long newDeptId) {
        return ResponseEntity.ok(ticketService.reassignTicket(ticketId, fromDeptId, newDeptId));
    }

    // Ticket kapatma
    @PutMapping("/{ticketId}/close")
    public ResponseEntity<TicketResponse> close(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.closeTicket(ticketId));
    }

    // Departman iç ticket oluşturma
    @PostMapping("/{deptId}/tickets/internal")
    public ResponseEntity<TicketResponse> createInternal(@PathVariable Long deptId,
                                                         @RequestBody InternalTicketRequest req) {
        return ResponseEntity.ok(ticketService.createInternalTicket(req, deptId));
    }
}
