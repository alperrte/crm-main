package com.example.ticket_service.repository;

import com.example.ticket_service.entity.TicketEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    // Tüm ticketları (müşteri bilgisiyle birlikte) tarihe göre getirir
    @EntityGraph(attributePaths = {"creatorCustomer"})
    List<TicketEntity> findAllByOrderByCreatedDateDesc();
    // Belirli bir person (çalışan) tarafından oluşturulmuş tüm ticket’ları getirir.
    List<TicketEntity> findByCreatorPersonId(Long personId);
}
