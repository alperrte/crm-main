package com.example.person.controller;

import com.example.person.dto.request.AssignDepartmentRequest;
import com.example.person.entity.PersonEntity;
import com.example.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/persons")
@RequiredArgsConstructor
public class AdminController { // ✅ sınıf adı netleştirildi

    private final PersonRepository personRepository;

    // Aktif tüm personlar
    @GetMapping
    public ResponseEntity<List<PersonEntity>> getAllPersons() {
        return ResponseEntity.ok(personRepository.findByActiveTrue());
    }

    // Departman atanmamış personlar
    @GetMapping("/unassigned")
    public ResponseEntity<List<PersonEntity>> getUnassignedPersons() {
        return ResponseEntity.ok(personRepository.findByActiveTrueAndDepartmentIdIsNull());
    }

    // Departmana göre liste
    @GetMapping("/department/{depId}")
    public ResponseEntity<List<PersonEntity>> getByDepartment(@PathVariable Long depId) {
        return ResponseEntity.ok(personRepository.findByDepartmentId(depId));
    }

    // Departman ata
    @PutMapping("/{id}/department")
    public ResponseEntity<PersonEntity> assignDepartment(@PathVariable Long id,
                                                         @RequestBody AssignDepartmentRequest req) {
        return personRepository.findById(id)
                .map(p -> {
                    p.setDepartmentId(req.getDepartmentId());
                    return ResponseEntity.ok(personRepository.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
