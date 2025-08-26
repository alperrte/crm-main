// dto/response/TicketResponse.java
package com.example.ticket_service.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record TicketResponse(
        Long id,
        String customerEmail,
        String customerName,
        String customerSurname,
        String customerPhone,
        String issue,
        String priority,
        Boolean active,
        LocalDateTime createdDate,

        // 🔹 yeni alanlar
        String status,
        Long departmentId
) {}
