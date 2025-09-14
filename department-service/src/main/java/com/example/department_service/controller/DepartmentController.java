package com.example.department_service.controller;

import com.example.department_service.dto.request.DepartmentRequest;
import com.example.department_service.dto.response.DepartmentResponse;
import com.example.department_service.data.entity.DepartmentEntity;
import com.example.department_service.data.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    // Tüm departmanları getir
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        List<DepartmentEntity> departments = departmentService.getAllDepartments();
        List<DepartmentResponse> response = departments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ID ile tek departman getir
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Yeni departman oluştur
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@RequestBody DepartmentRequest request) {
        DepartmentEntity entity = convertToEntity(request);
        DepartmentEntity created = departmentService.createDepartment(entity);
        return ResponseEntity.ok(convertToResponse(created));
    }

    // Departman güncelle
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentRequest request) {

        return departmentService.updateDepartment(id, convertToEntity(request))
                .map(updated -> ResponseEntity.ok(convertToResponse(updated)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Departman sil (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    // Entity → Response DTO
    private DepartmentResponse convertToResponse(DepartmentEntity entity) {
        return DepartmentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .parentDepartmentId(entity.getParentDepartmentId())
                .active(entity.getActive())
                .deleted(entity.getDeleted())
                .updated(entity.getUpdated())
                .build();
    }

    // Request DTO → Entity
    private DepartmentEntity convertToEntity(DepartmentRequest dto) {
        return DepartmentEntity.builder()
                .name(dto.getName())
                .parentDepartmentId(dto.getParentDepartmentId())
                .updatedUserId(dto.getUpdatedUserId())
                .active(true)   // yeni departman → default aktif
                .deleted(false)
                .updated(false)
                .build();
    }
}
