package com.example.department_service.data.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="department_id")
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="parent_department_id")
    private Long parentDepartmentId;

    @Column(name="is_active")
    private Boolean active;

    @Column(name="is_deleted")
    private Boolean deleted;

    @Column(name="deleted_user_id")
    private Long deletedUserId;

    @Column(name="is_updated")
    private Boolean updated;

    @Column(name="updated_user_id")
    private Long updatedUserId;

}