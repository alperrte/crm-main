package com.example.person.repository;

import com.example.person.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    // Belirli departmandaki personları getir
    List<PersonEntity> findByDepartmentId(Long departmentId);
    // Sadece aktif personları getir
    List<PersonEntity> findByActiveTrue();
    // Departman atanmamış aktif personları getir (admin tarafı için)
    List<PersonEntity> findByActiveTrueAndDepartmentIdIsNull();
    // Email ile person bul (UserPage -> /me için gerekli)
    Optional<PersonEntity> findByEmail(String email);
}
