package com.example.ticket_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InternalTicketRequest(
        @NotBlank String issue,
        @NotBlank String priority,   // LOW | MEDIUM | HIGH
        Integer categoryId,           // opsiyonel (istenirse kategori üzerinden yönlendirme yapılabilir)
        Long personId
) {}
