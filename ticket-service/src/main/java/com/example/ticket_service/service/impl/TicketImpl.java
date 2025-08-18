package com.example.ticket_service.service.impl;

import com.example.ticket_service.dto.request.TicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.entity.TicketEntity;
import com.example.ticket_service.repository.TicketRepository;
import com.example.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    @Override
    public TicketResponse createTicket(TicketRequest req) {
        validateRequest(req);

        boolean isEmployee = determineEmployeeFlag(req);

        TicketEntity entity = TicketEntity.builder()
                .issue(req.getIssue())
                .priority(req.getPriority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .employee(isEmployee)
                .creatorCustomerId(req.getCreatorCustomerId())
                .creatorPersonId(req.getCreatorPersonId())
                .build();

        TicketEntity saved = ticketRepository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Yardımcı metodlar ---

    private void validateRequest(TicketRequest req) {
        if (req.getIssue() == null || req.getIssue().isBlank()) {
            throw new IllegalArgumentException("issue zorunludur");
        }
        if (req.getPriority() == null || req.getPriority().isBlank()) {
            throw new IllegalArgumentException("priority zorunludur");
        }

        boolean hasCustomer = req.getCreatorCustomerId() != null;
        boolean hasPerson = req.getCreatorPersonId() != null;
        if (hasCustomer == hasPerson) {
            throw new IllegalArgumentException("creatorCustomerId veya creatorPersonId değerlerinden yalnızca birini vermelisiniz");
        }
    }

    private boolean determineEmployeeFlag(TicketRequest req) {
        boolean hasCustomer = req.getCreatorCustomerId() != null;
        boolean hasPerson = req.getCreatorPersonId() != null;

        boolean isEmployee = (req.getEmployee() != null)
                ? req.getEmployee()
                : hasPerson;

        if (isEmployee && !hasPerson) {
            throw new IllegalArgumentException("employee=true iken creatorPersonId zorunludur");
        }
        if (!isEmployee && !hasCustomer) {
            throw new IllegalArgumentException("employee=false iken creatorCustomerId zorunludur");
        }
        return isEmployee;
    }

    private TicketResponse mapToResponse(TicketEntity saved) {
        return TicketResponse.builder()
                .id(saved.getId())
                .issue(saved.getIssue())
                .priority(saved.getPriority())
                .active(saved.getActive())
                .createdDate(saved.getCreatedDate())
                .closedDate(saved.getClosedDate())
                .employee(saved.getEmployee())
                .creatorCustomerId(saved.getCreatorCustomerId())
                .creatorPersonId(saved.getCreatorPersonId())
                .build();
    }
}
