package com.example.person.repository;

import com.example.person.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    List<PersonEntity> findByDepartmentId(Long departmentId);

    List<PersonEntity> findByActiveTrue();

    // ✅ EKLENDİ: admin tarafında "Departmansız" liste
    List<PersonEntity> findByActiveTrueAndDepartmentIdIsNull(); // ✅ EKLENDİ
}
