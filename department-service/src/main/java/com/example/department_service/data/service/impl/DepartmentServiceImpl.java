package com.example.department_service.data.service.impl;

import com.example.department_service.data.entity.DepartmentEntity;
import com.example.department_service.data.repository.DepartmentRepository;
import com.example.department_service.data.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentEntity> getAllDepartments() {
        return departmentRepository.findAllByDeletedFalse();
    }

    @Override
    public Optional<DepartmentEntity> getDepartmentById(Long id) {
        return departmentRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public DepartmentEntity createDepartment(DepartmentEntity department) {
        return departmentRepository.save(department);
    }

    @Override
    public Optional<DepartmentEntity> updateDepartment(Long id, DepartmentEntity updatedDepartment) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .map(existingDepartment -> {
                    existingDepartment.setName(updatedDepartment.getName());
                    existingDepartment.setParentDepartmentId(updatedDepartment.getParentDepartmentId());
                    existingDepartment.setUpdated(true);
                    existingDepartment.setUpdatedUserId(updatedDepartment.getUpdatedUserId());
                    return departmentRepository.save(existingDepartment);
                });
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.findByIdAndDeletedFalse(id)
                .ifPresent(department -> {
                    department.setDeleted(true);
                    department.setDeletedUserId(1L); //
                    departmentRepository.save(department);
                });
    }
}