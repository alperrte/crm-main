package com.example.ticket_service.dto.request;

import lombok.Data;

/**
 * Ticket oluşturma isteği.
 * Kurallar:
 * - Yalnızca bir tanesi dolu olmalı: creatorCustomerId XOR creatorPersonId
 * - employee=true => creatorPersonId zorunlu
 * - employee=false => creatorCustomerId zorunlu
 */
@Data
public class TicketRequest {
    private String issue;
    private String priority;
    /** Çalışan tarafından mı oluşturuldu? (NULL ise id’lere göre otomatik belirlenir) */
    private Boolean employee;

    private Long creatorCustomerId;
    private Long creatorPersonId;
}
