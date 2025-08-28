package com.example.ticket_service.service;

import com.example.ticket_service.dto.request.InternalTicketRequest;
import com.example.ticket_service.dto.request.PublicTicketRequest;
import com.example.ticket_service.dto.response.TicketResponse;

import java.util.List;

/**
 * Ticket işlemlerini yöneten servis arayüzü.
 */
public interface TicketService {

    /**
     * Müşteri tarafından açılan ticket oluşturur.
     */
    TicketResponse createPublicTicket(PublicTicketRequest req);

    /**
     * Tüm ticketları listeler (sıralı).
     */
    List<TicketResponse> listAllTickets();

    // === Departman işlemleri ===

    /**
     * Belirtilen departmandaki ticketları listeler.
     */
    List<TicketResponse> listTicketsByDepartment(Long deptId);

    /**
     * Bir ticketı departman havuzundan üstlenir.
     */
    TicketResponse takeTicket(Long ticketId, Long deptId);

    /**
     * Bir ticketı başka departmana devreder.
     */
    TicketResponse reassignTicket(Long ticketId, Long fromDeptId, Long toDeptId);

    /**
     * Bir ticketı kapatır.
     */
    TicketResponse closeTicket(Long ticketId);

    /**
     * Departman içi (internal) ticket oluşturur.
     */
    TicketResponse createInternalTicket(InternalTicketRequest req);

    // === Kullanıcının kendi listeleri ===

    /**
     * Kullanıcının üstlendiği aktif ticketlar.
     */
    List<TicketResponse> listMyAssigned(Long personId);

    /**
     * Kullanıcının kapattığı ticketlar.
     */
    List<TicketResponse> listMyClosed(Long personId);

    /**
     * Kullanıcının devrettiği ticketlar.
     */
    List<TicketResponse> listMyTransferred(Long personId);
}
