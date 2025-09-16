package com.example.ticket_service.repository;

import com.example.ticket_service.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    // Aktif olan kategorileri bulur ve alanına göre alfabetik sıralar.
    List<CategoryEntity> findByActiveTrueOrderByDisplayNameAsc();

    // ✅ Departman ID’ye göre kategori bul
    Optional<CategoryEntity> findByTargetDepartmentId(Long targetDepartmentId);
}
