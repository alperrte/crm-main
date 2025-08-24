// repository/CategoryRepository.java
package com.example.ticket_service.repository;

import com.example.ticket_service.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    List<CategoryEntity> findByActiveTrueOrderByDisplayNameAsc();
}
