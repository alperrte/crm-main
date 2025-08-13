package com.example.department_service.data.repository;

import com.example.department_service.data.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity,Long> {

    // Silinmemiş tüm departmanları getirir
    List<DepartmentEntity> findAllByDeletedFalse();

    // Belirli bir ID'ye sahip ve silinmemiş departmanı getirir
    Optional<DepartmentEntity> findByIdAndDeletedFalse(Long id);


}
