package com.example.ticket_service.repository;

import com.example.ticket_service.entity.TicketAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface TicketAssignmentRepository extends JpaRepository<TicketAssignmentEntity, Long> {
    // Belirli bir departmana ait tüm ticket atamalarını döner.
    List<TicketAssignmentEntity> findByDepartmentId(Long deptId);
    // Belirli bir ticket’a ait tüm atamaları döner.
    List<TicketAssignmentEntity> findByTicketId(Long ticketId);
    // Belirli bir ticket+departman kombinasyonu için tek bir atamayı döner.
    Optional<TicketAssignmentEntity> findByTicketIdAndDepartmentId(Long ticketId, Long deptId);
    // Havuz (departmana atanmış, açık ve kimse üstlenmemiş)
    @Query("""
select a from TicketAssignmentEntity a
where a.departmentId = :deptId
and a.status = 'OPEN'
and a.personId is null
and a.ticket.active = true
""")
    List<TicketAssignmentEntity> findPool(@Param("deptId") Long deptId);
    // Üstlendiklerim
    @Query("""
select a from TicketAssignmentEntity a
where a.personId = :personId
and a.status = 'IN_PROGRESS'
and a.ticket.active = true
""")
    List<TicketAssignmentEntity> findMyAssigned(@Param("personId") Long personId);
    // Kapattıklarım
    @Query("""
select a from TicketAssignmentEntity a
where a.personId = :personId
and (a.completedDate is not null or a.status = 'DONE')
""")
    List<TicketAssignmentEntity> findMyClosed(@Param("personId") Long personId);
    // Devrettiklerim (devreden kişi person_id olarak işlenir)
    List<TicketAssignmentEntity> findByPersonIdAndStatusOrderByCompletedDateDesc(Long personId, String status);
    // Yarış koşulları için kilitleyerek çek
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
select a from TicketAssignmentEntity a
where a.ticket.id = :ticketId and a.departmentId = :deptId
""")
    Optional<TicketAssignmentEntity> findForUpdate(@Param("ticketId") Long ticketId,
                                                   @Param("deptId") Long deptId);
}