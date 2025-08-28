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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;

    // ✅ Person-service adresi
    private static final String PERSON_SERVICE_URL = "http://localhost:8082/api/persons/";

    private final RestTemplate restTemplate = new RestTemplate();

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
                .employee(false) // müşteri açtı
                .build();
        ticket = ticketRepository.save(ticket);

        CategoryEntity category = categoryRepository.findById(req.categoryId().intValue())
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
                .filter(a -> !"TRANSFERRED".equals(a.getStatus()))
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

        a.setStatus("IN_PROGRESS");
        a.setAssignedDate(LocalDateTime.now());
        a.setPersonId(personId);
        a.setDepartmentId(null);

        assignmentRepository.save(a);
        return toResponse(a.getTicket());
    }

    // === Devret ===
    @Override
    @Transactional
    public TicketResponse reassignTicket(Long ticketId, Long fromDeptId, Long toDeptId) {
        TicketAssignmentEntity oldAssign = assignmentRepository.findByTicketId(ticketId).stream()
                .max(Comparator.comparing(
                        a -> Optional.ofNullable(a.getAssignedDate()).orElse(LocalDateTime.MIN)))
                .orElseThrow(() -> new IllegalArgumentException("Ticket için assignment bulunamadı"));

        Long actor = Optional.ofNullable(currentPersonId())
                .orElseThrow(() -> new IllegalStateException("Devreden personId bulunamadı!"));

        oldAssign.setPersonId(actor);
        oldAssign.setStatus("TRANSFERRED");
        oldAssign.setCompletedDate(LocalDateTime.now());
        oldAssign.setDepartmentId(null);
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
    public TicketResponse createInternalTicket(InternalTicketRequest req) {
        Long personId = req.personId() != null ? req.personId() : currentPersonId();
        if (personId == null) {
            throw new IllegalStateException("Internal ticket açan personId bulunamadı!");
        }
        if (req.departmentId() == null) {
            throw new IllegalArgumentException("Departman seçilmesi zorunludur");
        }

        TicketEntity ticket = TicketEntity.builder()
                .issue(req.issue())
                .priority(req.priority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .creatorPersonId(personId)
                .employee(true)
                .build();
        ticket = ticketRepository.save(ticket);

        TicketAssignmentEntity ta = TicketAssignmentEntity.builder()
                .ticket(ticket)
                .departmentId(req.departmentId())
                .status("OPEN")
                .assignedDate(LocalDateTime.now())
                .inPool(false)
                .build();
        assignmentRepository.save(ta);

        return toResponse(ticket);
    }

    // === User Ticket ===
    @Override
    @Transactional
    public TicketResponse createUserTicket(String issue, String priority, Long categoryId, Long personId) {
        if (personId == null) {
            throw new IllegalStateException("User ticket açan personId bulunamadı!");
        }

        TicketEntity ticket = TicketEntity.builder()
                .issue(issue)
                .priority(priority)
                .active(true)
                .createdDate(LocalDateTime.now())
                .creatorPersonId(personId)
                .employee(true)
                .build();
        ticket = ticketRepository.save(ticket);

        if (categoryId != null) {
            CategoryEntity category = categoryRepository.findById(categoryId.intValue())
                    .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı"));

            TicketAssignmentEntity ta = TicketAssignmentEntity.builder()
                    .ticket(ticket)
                    .departmentId(category.getTargetDepartmentId())
                    .status("OPEN")
                    .assignedDate(LocalDateTime.now())
                    .inPool(false)
                    .build();
            assignmentRepository.save(ta);
        }

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

    @Override
    public List<TicketResponse> listMyTicketsByPerson(Long personId) {
        return ticketRepository.findByCreatorPersonId(personId).stream()
                .map(this::toResponse)
                .toList();
    }

    // === Map ===
    private TicketResponse toResponse(TicketEntity t) {
        List<TicketAssignmentEntity> all = assignmentRepository.findByTicketId(t.getId());

        TicketAssignmentEntity latest = all.stream()
                .max(Comparator.comparing(
                        a -> Optional.ofNullable(a.getAssignedDate()).orElse(LocalDateTime.MIN)))
                .orElse(null);

        TicketAssignmentEntity lastTransferred = all.stream()
                .filter(a -> "TRANSFERRED".equals(a.getStatus()))
                .max(Comparator.comparing(
                        a -> Optional.ofNullable(a.getCompletedDate()).orElse(LocalDateTime.MIN)))
                .orElse(null);

        // ✅ Ticket'ı açan çalışanın bilgileri
        String creatorEmail = null;
        String creatorName = null;
        String creatorSurname = null;
        if (t.getCreatorPersonId() != null) {
            try {
                Map<?, ?> person = restTemplate.getForObject(
                        PERSON_SERVICE_URL + t.getCreatorPersonId(),
                        Map.class
                );
                if (person != null) {
                    creatorEmail = (String) person.get("email");
                    creatorName = (String) person.get("name");
                    creatorSurname = (String) person.get("surname");
                }
            } catch (Exception e) {
                log.warn("Person-service (creator) çağrısı başarısız oldu: {}", e.getMessage());
            }
        }

        // ✅ Üstlenen çalışanın bilgileri
        String assigneeEmail = null;
        String assigneeName = null;
        String assigneeSurname = null;
        if (latest != null && latest.getPersonId() != null) {
            try {
                Map<?, ?> person = restTemplate.getForObject(
                        PERSON_SERVICE_URL + latest.getPersonId(),
                        Map.class
                );
                if (person != null) {
                    assigneeEmail = (String) person.get("email");
                    assigneeName = (String) person.get("name");
                    assigneeSurname = (String) person.get("surname");
                }
            } catch (Exception e) {
                log.warn("Person-service (assignee) çağrısı başarısız oldu: {}", e.getMessage());
            }
        }

        return TicketResponse.builder()
                .id(t.getId())
                // müşteri bilgileri
                .customerEmail(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getEmail() : null)
                .customerName(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getName() : null)
                .customerSurname(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getSurname() : null)
                .customerPhone(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getPhone() : null)
                // çalışan bilgileri (ticket açan person)
                .creatorPersonEmail(creatorEmail)
                .creatorPersonName(creatorName)
                .creatorPersonSurname(creatorSurname)
                // temel ticket
                .issue(t.getIssue())
                .priority(t.getPriority())
                .active(t.getActive())
                .createdDate(t.getCreatedDate())
                .closedDate(t.getClosedDate())
                .status(latest != null ? latest.getStatus() : null)
                .departmentId(latest != null ? latest.getDepartmentId() : null)
                // opsiyonel
                .assigneePersonId(latest != null ? latest.getPersonId() : null)
                .employee(t.getEmployee())
                .fromDepartmentId(lastTransferred != null ? lastTransferred.getDepartmentId() : null)
                .toDepartmentId(latest != null ? latest.getDepartmentId() : null)
                // üstlenen kişi bilgileri
                .assigneeEmail(assigneeEmail)
                .assigneeName(assigneeName)
                .assigneeSurname(assigneeSurname)
                .build();
    }
}
