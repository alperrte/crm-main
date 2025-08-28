package com.example.ticket_service.dto.request;

public record UserTicketRequest(
        String issue,
        String priority,
        Long categoryId
) {}