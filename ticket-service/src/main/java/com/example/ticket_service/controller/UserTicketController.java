// src/main/java/com/example/ticket_service/controller/UserTicketController.java
package com.example.ticket_service.controller;

import com.example.ticket_service.dto.request.UserTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.security.jwt.JwtUtil;
import com.example.ticket_service.service.TicketService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-panel")
@RequiredArgsConstructor
@Slf4j
public class UserTicketController {

    private final TicketService ticketService;
    private final JwtUtil jwtUtil;

    // USER ticket açar
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> create(@RequestBody UserTicketRequest req,
                                                 Authentication auth) {
        String token = (String) auth.getCredentials();
        Claims claims = jwtUtil.parse(token);
        Long personId = claims.get("personId", Long.class);

        log.info("➡️ USER ticket açıyor personId={}", personId);

        return ResponseEntity.ok(
                ticketService.createUserTicket(req.issue(), req.priority(), req.categoryId(), personId)
        );
    }

    // USER kendi açtığı ticketları görür
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponse>> myTickets(Authentication auth) {
        String token = (String) auth.getCredentials();
        Claims claims = jwtUtil.parse(token);
        Long personId = claims.get("personId", Long.class);

        log.info("➡️ USER ticket listesi personId={}", personId);

        return ResponseEntity.ok(ticketService.listMyTicketsByPerson(personId));
    }
}
