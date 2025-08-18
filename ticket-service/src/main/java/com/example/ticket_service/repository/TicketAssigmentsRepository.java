// src/main/java/com/example/ticket_service/repository/TicketAssigmentsRepository.java
package com.example.ticket_service.repository;

import com.example.ticket_service.entity.TicketAssigmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAssigmentsRepository extends JpaRepository<TicketAssigmentsEntity,Long> {

    /** bir ticket'a ait tüm assignment'lar */
    List<TicketAssigmentsEntity> findByTicketId(Long ticketId);

    /** tek “pool” kaydı kuralını kontrol etmek için */
    Optional<TicketAssigmentsEntity> findByTicketIdAndInPoolTrue(Long ticketId);

    /** departmana atanmış işler */
    List<TicketAssigmentsEntity> findByDepartmentId(Long departmentId);

    /** kişiye atanmış işler */
    List<TicketAssigmentsEntity> findByPersonId(Long personId);

    /** durum bazlı filtre (OPEN / IN_PROGRESS / DONE) */
    List<TicketAssigmentsEntity> findByStatus(String status);

}
