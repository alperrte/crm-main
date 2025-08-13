package com.example.person.repository;

import com.example.person.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<PersonEntity,Long> {
    // Department'a göre person listesi
    List<PersonEntity> findByDepartmentId(Long departmentId);

    // Aktif personları listele
    List<PersonEntity> findByActiveTrue();

}
