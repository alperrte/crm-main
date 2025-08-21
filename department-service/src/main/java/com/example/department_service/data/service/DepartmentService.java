package com.example.department_service.data.service;

import com.example.department_service.data.entity.DepartmentEntity;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    List<DepartmentEntity> getAllDepartments();

    Optional<DepartmentEntity> getDepartmentById(Long id);

    DepartmentEntity createDepartment(DepartmentEntity department);

    Optional<DepartmentEntity> updateDepartment(Long id, DepartmentEntity department);

    void deleteDepartment(Long id);
}