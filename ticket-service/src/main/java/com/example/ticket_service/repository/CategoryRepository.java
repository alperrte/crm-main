package com.example.ticket_service.repository;

import com.example.ticket_service.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    // Aktif olan kategorileri bulur ve alanına göre alfabetik sıralar.
    List<CategoryEntity> findByActiveTrueOrderByDisplayNameAsc();
}
