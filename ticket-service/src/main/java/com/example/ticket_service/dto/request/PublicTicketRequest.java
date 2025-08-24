// dto/request/PublicTicketRequest.java
package com.example.ticket_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicTicketRequest(
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone,
        @NotBlank String issue,
        @NotBlank String priority,   // "LOW" | "MEDIUM" | "HIGH" (serbest string)
        @NotNull Integer categoryId  // yönlendirme amaçlı
) {}
