// entity/CategoryEntity.java
package com.example.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "category_key", nullable = false, length = 64)
    private String key;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "target_department_id")
    private Long targetDepartmentId;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
