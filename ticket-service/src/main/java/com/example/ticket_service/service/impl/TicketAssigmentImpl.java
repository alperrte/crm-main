package com.example.ticket_service.service.impl;

import com.example.ticket_service.dto.request.TicketAssigmentRequest;
import com.example.ticket_service.dto.response.TicketAssigmentResponse;
import com.example.ticket_service.entity.TicketAssigmentsEntity;
import com.example.ticket_service.repository.TicketAssigmentsRepository;
import com.example.ticket_service.service.TicketAssigmentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketAssigmentImpl implements TicketAssigmentsService {

    private final TicketAssigmentsRepository assignmentRepository;

    @Transactional
    @Override
    public TicketAssigmentResponse assignTicket(TicketAssigmentRequest request) {
        if (request.getTicketId() == null) {
            throw new IllegalArgumentException("ticketId zorunludur");
        }
        boolean pool = Boolean.TRUE.equals(request.getPool());
        Long depId = request.getDepartmentId();
        Long perId = request.getPersonId();

        if (pool) {
            // Havuz ataması ise hedef NULL olmalı
            depId = null;
            perId = null;
            // Aynı ticket için ikinci bir pool kaydı oluşturulamaz
            Optional<TicketAssigmentsEntity> existingPool = assignmentRepository.findByTicketIdAndInPoolTrue(request.getTicketId());
            if (existingPool.isPresent()) {
                throw new IllegalStateException("Bu ticket için zaten bir pool kaydı var");
            }
        } else {
            // Havuz değilse tam bir hedef olmalı (yalnızca biri dolu)
            boolean depProvided = depId != null;
            boolean perProvided = perId != null;
            if (depProvided == perProvided) {
                throw new IllegalArgumentException("pool=false iken departmentId veya personId değerlerinden yalnızca birini vermelisiniz");
            }
        }

        String status = (request.getStatus() == null || request.getStatus().isBlank())
                ? "OPEN"
                : request.getStatus();

        TicketAssigmentsEntity entity = TicketAssigmentsEntity.builder()
                .ticketId(request.getTicketId())
                .inPool(pool)
                .departmentId(depId)
                .personId(perId)
                .assigned(LocalDateTime.now())
                .status(status)
                .build();

        TicketAssigmentsEntity saved = assignmentRepository.save(entity);

        return TicketAssigmentResponse.builder()
                .id(saved.getId())
                .ticketId(saved.getTicketId())
                .pool(saved.getInPool())
                .departmentId(saved.getDepartmentId())
                .personId(saved.getPersonId())
                .assigned(saved.getAssigned())
                .completed(saved.getCompleted())
                .status(saved.getStatus())
                .build();
    }

    @Override
    public List<TicketAssigmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(a -> TicketAssigmentResponse.builder()
                        .id(a.getId())
                        .ticketId(a.getTicketId())
                        .pool(a.getInPool())
                        .departmentId(a.getDepartmentId())
                        .personId(a.getPersonId())
                        .assigned(a.getAssigned())
                        .completed(a.getCompleted())
                        .status(a.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
