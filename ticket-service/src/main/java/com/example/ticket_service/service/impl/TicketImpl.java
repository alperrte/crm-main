package com.example.ticket_service.service.impl;

import com.example.ticket_service.dto.request.InternalTicketRequest;
import com.example.ticket_service.dto.request.PublicTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.entity.*;
import com.example.ticket_service.repository.*;
import com.example.ticket_service.security.jwt.JwtUtil;
import com.example.ticket_service.service.TicketService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;

    // ----------------- VAR OLAN -----------------
    @Override
    @Transactional
    public TicketResponse createPublicTicket(PublicTicketRequest req) {
        CustomerEntity customer = customerRepository.findByEmail(req.email())
                .map(c -> {
                    c.setName(req.firstName());
                    c.setSurname(req.lastName());
                    c.setPhone(req.phone());
                    return c;
                })
                .orElseGet(() -> CustomerEntity.builder()
                        .email(req.email())
                        .name(req.firstName())
                        .surname(req.lastName())
                        .phone(req.phone())
                        .build());

        customer = customerRepository.save(customer);

        TicketEntity ticket = TicketEntity.builder()
                .issue(req.issue())
                .priority(req.priority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .employee(false)
                .creatorCustomer(customer)
                .build();
        ticket = ticketRepository.save(ticket);

        CategoryEntity category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı"));

        TicketAssignmentEntity ta = TicketAssignmentEntity.builder()
                .ticket(ticket)
                .assignedDate(LocalDateTime.now())
                .status("OPEN")
                .build();

        if (category.getTargetDepartmentId() != null) {
            ta.setInPool(false);
            ta.setDepartmentId(category.getTargetDepartmentId());
        } else {
            ta.setInPool(true);
        }
        assignmentRepository.save(ta);

        return toResponse(ticket);
    }

    @Override
    public List<TicketResponse> listAllTickets() {
        return ticketRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------- YENİ EKLENENLER -----------------
    @Override
    public List<TicketResponse> listTicketsByDepartment(Long deptId) {
        return assignmentRepository.findByDepartmentId(deptId).stream()
                .map(TicketAssignmentEntity::getTicket)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse takeTicket(Long ticketId, Long deptId) {
        TicketAssignmentEntity assignment = assignmentRepository
                .findByTicketIdAndDepartmentId(ticketId, deptId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bu departmana atanmadı"));

        assignment.setStatus("IN_PROGRESS");
        assignment.setAssignedDate(LocalDateTime.now());
        assignmentRepository.save(assignment);

        return toResponse(assignment.getTicket());
    }

    @Override
    @Transactional
    public TicketResponse reassignTicket(Long ticketId, Long fromDeptId, Long toDeptId) {
        TicketAssignmentEntity oldAssign = assignmentRepository
                .findByTicketIdAndDepartmentId(ticketId, fromDeptId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bu departmana atanmadı"));

        oldAssign.setStatus("TRANSFERRED");
        assignmentRepository.save(oldAssign);

        TicketAssignmentEntity newAssign = TicketAssignmentEntity.builder()
                .ticket(oldAssign.getTicket())
                .departmentId(toDeptId)
                .status("OPEN")
                .assignedDate(LocalDateTime.now())
                .inPool(false)
                .build();
        assignmentRepository.save(newAssign);

        return toResponse(oldAssign.getTicket());
    }

    @Override
    @Transactional
    public TicketResponse closeTicket(Long ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bulunamadı"));

        ticket.setActive(false);
        ticket.setClosedDate(LocalDateTime.now());
        ticketRepository.save(ticket);

        assignmentRepository.findByTicketId(ticketId)
                .forEach(a -> {
                    a.setStatus("DONE");
                    a.setCompletedDate(LocalDateTime.now());
                    assignmentRepository.save(a);
                });

        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse createInternalTicket(InternalTicketRequest req, Long deptId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long personId = null;
        try {
            String token = auth.getCredentials().toString(); // ✅ artık token geliyor
            Claims claims = jwtUtil.parse(token);
            personId = claims.get("personId", Long.class);   // ✅ userId değil, personId
            log.info("Internal ticket oluşturuluyor → user={}, personId={}", auth.getName(), personId);
        } catch (Exception e) {
            log.warn("⚠️ Token’dan personId okunamadı, null kalıyor", e);
        }

        if (personId == null) {
            throw new IllegalStateException("Internal ticket açan personId bulunamadı!");
        }

        TicketEntity ticket = TicketEntity.builder()
                .issue(req.issue())
                .priority(req.priority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .employee(true)
                .creatorPersonId(personId)   // ✅ artık dolu
                .build();
        ticket = ticketRepository.save(ticket);

        TicketAssignmentEntity ta = TicketAssignmentEntity.builder()
                .ticket(ticket)
                .departmentId(deptId)
                .status("OPEN")
                .assignedDate(LocalDateTime.now())
                .inPool(false)
                .build();
        assignmentRepository.save(ta);

        return toResponse(ticket);
    }

    // ----------------- HELPER -----------------
    private TicketResponse toResponse(TicketEntity t) {
        TicketAssignmentEntity assignment = assignmentRepository.findByTicketId(t.getId())
                .stream()
                .reduce((first, second) -> second) // son assignment
                .orElse(null);

        return TicketResponse.builder()
                .id(t.getId())
                .customerEmail(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getEmail() : null)
                .customerName(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getName() : null)
                .customerSurname(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getSurname() : null)
                .customerPhone(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getPhone() : null)
                .issue(t.getIssue())
                .priority(t.getPriority())
                .active(t.getActive())
                .createdDate(t.getCreatedDate())
                .status(assignment != null ? assignment.getStatus() : null)
                .departmentId(assignment != null ? assignment.getDepartmentId() : null)
                .build();
    }
}
