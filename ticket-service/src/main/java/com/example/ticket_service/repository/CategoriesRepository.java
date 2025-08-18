package com.example.ticket_service.repository;

import com.example.ticket_service.entity.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriesRepository extends JpaRepository<CategoriesEntity, Long> {

    /** Drop-down için aktif kategoriler */
    List<CategoriesEntity> findByActiveTrue();

    /** Key ile tekil erişim */
    Optional<CategoriesEntity> findByKey(String key);

    /** Görünen ada göre arama (opsiyonel) */
    List<CategoriesEntity> findByDisplayNameContainingIgnoreCase(String q);
}
