// service/impl/TicketServiceImpl.java
package com.example.ticket_service.service.impl;

import com.example.ticket_service.dto.request.PublicTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;
import com.example.ticket_service.entity.*;
import com.example.ticket_service.repository.*;
import com.example.ticket_service.service.TicketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final TicketAssignmentRepository assignmentRepository;

    @Override
    @Transactional
    public TicketResponse createPublicTicket(PublicTicketRequest req) {
        // 1) Müşteriyi email ile bul/yoksa oluştur-güncelle
        CustomerEntity customer = customerRepository.findByEmail(req.email())
                .map(c -> {
                    // isim/telefon güncelleme (isteğe bağlı)
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

        // 2) Ticket oluştur
        TicketEntity ticket = TicketEntity.builder()
                .issue(req.issue())
                .priority(req.priority())
                .active(true)
                .createdDate(LocalDateTime.now())
                .employee(false) // public
                .creatorCustomer(customer)
                .build();
        ticket = ticketRepository.save(ticket);

        // 3) Kategoriye göre assignment aç
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
            ta.setInPool(true); // genel havuz
        }
        assignmentRepository.save(ta);

        // 4) Response
        return TicketResponse.builder()
                .id(ticket.getId())
                .customerEmail(customer.getEmail())
                .customerName(customer.getName())
                .customerSurname(customer.getSurname())
                .customerPhone(customer.getPhone())
                .issue(ticket.getIssue())
                .priority(ticket.getPriority())
                .active(ticket.getActive())
                .createdDate(ticket.getCreatedDate())
                .build();
    }

    @Override
    public List<TicketResponse> listAllTickets() {
        return ticketRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .customerEmail(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getEmail() : null)
                        .customerName(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getName() : null)
                        .customerSurname(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getSurname() : null)
                        .customerPhone(t.getCreatorCustomer() != null ? t.getCreatorCustomer().getPhone() : null)
                        .issue(t.getIssue())
                        .priority(t.getPriority())
                        .active(t.getActive())
                        .createdDate(t.getCreatedDate())
                        .build())
                .toList();
    }
}
