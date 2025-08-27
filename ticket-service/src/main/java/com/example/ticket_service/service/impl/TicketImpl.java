// TicketImpl.java
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;

    // === Helper: JWT'den personId oku ===
    private Long currentPersonId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        try {
            String token = String.valueOf(a.getCredentials());
            Claims c = jwtUtil.parse(token);
            return c.get("personId", Long.class);
        } catch (Exception e) {
            log.warn("personId claim okunamadı", e);
            return null;
        }
    }

    // === Public Ticket ===
    @Override
    @Transactional
    public TicketResponse createPublicTicket(PublicTicketRequest req) {
        CustomerEntity customer = customerRepository.findByEmail(req.email())
                .orElseGet(() -> customerRepository.save(
                        CustomerEntity.builder()
                                .email(req.email())
                                .name(req.firstName())
                                .surname(req.lastName())
                                .phone(req.phone())
                                .build()
                ));

        TicketEntity ticket = TicketEntity.builder()
                .issue(req.issue())
                .priority(req.priority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .creatorCustomer(customer)
                .employee(false) // 🔹 public ticket → müşteri açtı
                .build();
        ticket = ticketRepository.save(ticket);

        CategoryEntity category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı"));

        TicketAssignmentEntity ta = TicketAssignmentEntity.builder()
                .ticket(ticket)
                .departmentId(category.getTargetDepartmentId())
                .status("OPEN")
                .assignedDate(LocalDateTime.now())
                .inPool(false)
                .build();
        assignmentRepository.save(ta);

        return toResponse(ticket);
    }

    @Override
    public List<TicketResponse> listAllTickets() {
        return ticketRepository.findAllByOrderByCreatedDateDesc()
                .stream().map(this::toResponse).toList();
    }

    // === Departman Havuzu ===
    @Override
    public List<TicketResponse> listTicketsByDepartment(Long deptId) {
        return assignmentRepository.findByDepartmentId(deptId).stream()
                .map(TicketAssignmentEntity::getTicket)
                .map(this::toResponse)
                .toList();
    }

    // === Üstlen ===
    @Override
    @Transactional
    public TicketResponse takeTicket(Long ticketId, Long deptId) {
        TicketAssignmentEntity a = assignmentRepository.findForUpdate(ticketId, deptId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bu departmana atanmadı"));

        if (!"OPEN".equals(a.getStatus()) || a.getPersonId() != null) {
            throw new IllegalStateException("Ticket zaten alınmış/devredilmiş");
        }

        Long personId = Optional.ofNullable(currentPersonId())
                .orElseThrow(() -> new IllegalStateException("personId yok!"));

        log.info("🎯 takeTicket: ticketId={}, deptId={}, eski status={}, eski personId={}",
                ticketId, deptId, a.getStatus(), a.getPersonId());

        a.setStatus("IN_PROGRESS");
        a.setAssignedDate(LocalDateTime.now());
        a.setPersonId(personId);

        // 🔹 constraint gereği: is_in_pool=0 → personId dolu, departmentId NULL
        a.setDepartmentId(null);

        assignmentRepository.save(a);

        log.info("✅ Ticket üstlenildi: ticketId={}, personId={}, status={}",
                ticketId, personId, a.getStatus());

        return toResponse(a.getTicket());
    }

    // === Devret ===
    @Override
    @Transactional
    public TicketResponse reassignTicket(Long ticketId, Long fromDeptId, Long toDeptId) {
        TicketAssignmentEntity oldAssign = assignmentRepository.findForUpdate(ticketId, fromDeptId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bu departmana atanmadı"));

        Long actor = Optional.ofNullable(currentPersonId())
                .orElseThrow(() -> new IllegalStateException("Devreden personId bulunamadı!"));

        log.info("🎯 reassignTicket: ticketId={}, fromDeptId={}, toDeptId={}, eski status={}, eski personId={}",
                ticketId, fromDeptId, toDeptId, oldAssign.getStatus(), oldAssign.getPersonId());

        // devreden kişi set ediliyor
        oldAssign.setPersonId(actor);
        oldAssign.setStatus("TRANSFERRED");
        oldAssign.setCompletedDate(LocalDateTime.now());

        // 🔹 constraint gereği: TRANSFERRED assignment'ta departmentId NULL olmalı
        oldAssign.setDepartmentId(null);

        assignmentRepository.save(oldAssign);

        // yeni assignment: sadece departmanId dolu, personId boş → constraint uyumlu
        TicketAssignmentEntity newAssign = TicketAssignmentEntity.builder()
                .ticket(oldAssign.getTicket())
                .departmentId(toDeptId)
                .status("OPEN")
                .assignedDate(LocalDateTime.now())
                .inPool(false)
                .build();
        assignmentRepository.save(newAssign);

        log.info("✅ Ticket devredildi: ticketId={}, fromDeptId={}, toDeptId={}, actor={}",
                ticketId, fromDeptId, toDeptId, actor);

        return toResponse(oldAssign.getTicket());
    }

    // === Kapat ===
    @Override
    @Transactional
    public TicketResponse closeTicket(Long ticketId) {
        TicketEntity t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket bulunamadı"));

        if (Boolean.FALSE.equals(t.getActive())) return toResponse(t);

        t.setActive(false);
        t.setClosedDate(LocalDateTime.now());
        ticketRepository.save(t);

        assignmentRepository.findByTicketId(ticketId).forEach(a -> {
            if (!"DONE".equals(a.getStatus())) {
                a.setStatus("DONE");
            }
            if (a.getCompletedDate() == null) {
                a.setCompletedDate(LocalDateTime.now());
            }
            assignmentRepository.save(a);
        });

        return toResponse(t);
    }

    // === Internal Ticket ===
    @Override
    @Transactional
    public TicketResponse createInternalTicket(InternalTicketRequest req, Long deptId) {
        Long personId = Optional.ofNullable(currentPersonId())
                .orElseThrow(() -> new IllegalStateException("Internal ticket açan personId bulunamadı!"));

        TicketEntity ticket = TicketEntity.builder()
                .issue(req.issue())
                .priority(req.priority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .creatorPersonId(personId)
                .employee(true) // 🔹 internal ticket → personel açtı
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

    // === Benim Listelerim ===
    @Override
    public List<TicketResponse> listMyAssigned(Long personId) {
        return assignmentRepository.findMyAssigned(personId).stream()
                .map(TicketAssignmentEntity::getTicket)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> listMyClosed(Long personId) {
        return assignmentRepository.findMyClosed(personId).stream()
                .map(TicketAssignmentEntity::getTicket)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> listMyTransferred(Long personId) {
        return assignmentRepository.findByPersonIdAndStatusOrderByCompletedDateDesc(personId, "TRANSFERRED").stream()
                .map(TicketAssignmentEntity::getTicket)
                .sorted(Comparator.comparing(
                        t -> Optional.ofNullable(t.getClosedDate()).orElse(t.getCreatedDate()),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    // === Map ===
    private TicketResponse toResponse(TicketEntity t) {
        TicketAssignmentEntity assignment = assignmentRepository.findByTicketId(t.getId()).stream()
                .max(Comparator.comparing(
                        a -> Optional.ofNullable(a.getAssignedDate()).orElse(LocalDateTime.MIN)))
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
                .assigneePersonId(assignment != null ? assignment.getPersonId() : null)
                .employee(t.getEmployee()) // 🔹 burası eklendi
                .build();
    }
}
