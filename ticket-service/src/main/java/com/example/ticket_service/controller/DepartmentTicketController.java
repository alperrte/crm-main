package com.example.ticket_service.controller;

import com.example.ticket_service.dto.request.InternalTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentTicketController {

    private final TicketService ticketService;

    private void logAuth(String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.info("➡️ [{}] user={}, authorities={}", action, auth.getName(), auth.getAuthorities());
        } else {
            log.warn("➡️ [{}] Anonymous access!", action);
        }
    }

    // === Departmanın tüm ticketları ===
    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @GetMapping("/{deptId}/tickets")
    public ResponseEntity<List<TicketResponse>> ticketsByDepartment(@PathVariable Long deptId) {
        logAuth("ticketsByDepartment deptId=" + deptId);
        return ResponseEntity.ok(ticketService.listTicketsByDepartment(deptId));
    }

    // === Ticket üstlenme ===
    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @PutMapping("/tickets/{ticketId}/take")
    public ResponseEntity<TicketResponse> take(@PathVariable Long ticketId,
                                               @RequestParam(name = "deptId") Long deptId) {
        logAuth("take ticketId=" + ticketId + ", deptId=" + deptId);
        return ResponseEntity.ok(ticketService.takeTicket(ticketId, deptId));
    }

    // === Ticket devretme ===
    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @PutMapping("/tickets/{ticketId}/reassign/{newDeptId}")
    public ResponseEntity<TicketResponse> reassign(@PathVariable Long ticketId,
                                                   @RequestParam(name = "fromDeptId") Long fromDeptId,
                                                   @PathVariable Long newDeptId) {
        logAuth("reassign ticketId=" + ticketId + ", fromDeptId=" + fromDeptId + ", newDeptId=" + newDeptId);
        return ResponseEntity.ok(ticketService.reassignTicket(ticketId, fromDeptId, newDeptId));
    }

    // === Ticket kapatma ===
    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @PutMapping("/tickets/{ticketId}/close")
    public ResponseEntity<TicketResponse> close(@PathVariable Long ticketId) {
        logAuth("close ticketId=" + ticketId);
        return ResponseEntity.ok(ticketService.closeTicket(ticketId));
    }

    // === Departman iç ticket oluşturma ===
    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @PostMapping("/{deptId}/tickets/internal")
    public ResponseEntity<TicketResponse> createInternal(@PathVariable Long deptId,
                                                         @RequestBody InternalTicketRequest req) {
        logAuth("createInternal deptId=" + deptId);
        return ResponseEntity.ok(ticketService.createInternalTicket(req, deptId));
    }

    // === Kullanıcının kendi listeleri ===
    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @GetMapping("/me/assigned")
    public ResponseEntity<List<TicketResponse>> myAssigned(@RequestParam(name = "personId") Long personId) {
        logAuth("myAssigned personId=" + personId);
        return ResponseEntity.ok(ticketService.listMyAssigned(personId));
    }

    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @GetMapping("/me/closed")
    public ResponseEntity<List<TicketResponse>> myClosed(@RequestParam(name = "personId") Long personId) {
        logAuth("myClosed personId=" + personId);
        return ResponseEntity.ok(ticketService.listMyClosed(personId));
    }

    @PreAuthorize("hasAnyRole('PERSON','ADMIN')")
    @GetMapping("/me/transferred")
    public ResponseEntity<List<TicketResponse>> myTransferred(@RequestParam(name = "personId") Long personId) {
        logAuth("myTransferred personId=" + personId);
        return ResponseEntity.ok(ticketService.listMyTransferred(personId));
    }
}
