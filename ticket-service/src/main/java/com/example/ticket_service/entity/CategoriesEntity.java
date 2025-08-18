package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="categories")
public class CategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Long id;

    @Column(name="category_key")
    private String key;

    @Column(name="display_name")
    private String displayName;

    @Column(name="target_department_id")
    private int targetId;

    @Column(name="is_active")
    private Boolean active;

    @Column(name="created_date")
    private LocalDateTime created;

    @Column(name="updated_date")
    private LocalDateTime updated;

}
