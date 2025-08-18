// src/main/java/com/example/ticket_service/dto/request/TicketAssigmentRequest.java
package com.example.ticket_service.dto.request;

import lombok.Data;

/**
 * Bir ticket ataması için istek.
 * Not: pool=true ise departmentId ve personId NULL olmalıdır.
 * pool=false ise departmentId veya personId tam olarak birisi dolu olmalıdır.
 */
@Data
public class TicketAssigmentRequest {

    private Long ticketId;
    private Long departmentId;
    private Long personId;
    /** OPEN / IN_PROGRESS / DONE vb. Boş ise 'OPEN' kabul edilir. */
    private String status;
    /** Havuz (pool) ataması mı? */
    private Boolean pool;
}
