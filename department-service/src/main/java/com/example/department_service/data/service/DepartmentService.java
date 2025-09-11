package com.example.department_service.data.service;

import com.example.department_service.data.entity.DepartmentEntity;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    // Tüm Departmanları getir
    List<DepartmentEntity> getAllDepartments();

    //Departmanı ID'ye göre getir
    Optional<DepartmentEntity> getDepartmentById(Long id);

    //Sıfırdan bir departman oluştur
    DepartmentEntity createDepartment(DepartmentEntity department);

    //Mevcut bir departmanı güncelle
    Optional<DepartmentEntity> updateDepartment(Long id, DepartmentEntity department);

    //Mevcut bir departmanı id'ye göre sil
    // (soft delete olduğu için tamamen silmez silindi olarak işaretler)
    void deleteDepartment(Long id);
}
