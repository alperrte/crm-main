package com.example.ticket_service.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record TicketResponse(
        Long id,

        // Customer bilgileri (public başvurular için)
        String customerEmail,
        String customerName,
        String customerSurname,
        String customerPhone,

        // Temel ticket
        String issue,
        String priority, // LOW / MEDIUM / HIGH
        Boolean active,
        LocalDateTime createdDate,

        // Anlık durum
        String status, // OPEN | IN_PROGRESS | TRANSFERRED | DONE
        Long departmentId, // şu anki departman

        // Opsiyonel zengin alanlar
        Long assigneePersonId,       // üstlenen kişi
        Long transferredByPersonId,  // son devreden kişi
        String transferredByFullName, // devredenin adı

        Boolean employee
) {}
