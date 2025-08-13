package com.example.person.controller;

import com.example.person.dto.request.PersonRequestDto;
import com.example.person.dto.response.PersonResponseDto;
import com.example.person.entity.PersonEntity;
import com.example.person.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService; // ✅ Dependency Injection

    // ✅ 1. Tüm personlar (sadece aktifler)
    @GetMapping
    public ResponseEntity<List<PersonResponseDto>> getAllPersons() {
        List<PersonEntity> persons = personService.getAllPersons();

        List<PersonResponseDto> response = persons.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ✅ 2. Tek person getir
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDto> getPersonById(@PathVariable Long id) {
        Optional<PersonEntity> opt = personService.getPersonById(id);

        return opt.map(e -> ResponseEntity.ok(toResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ 3. Yeni person oluştur
    @PostMapping
    public ResponseEntity<PersonResponseDto> createPerson(@RequestBody PersonRequestDto req) {
        PersonEntity entity = toEntity(req);
        PersonEntity saved = personService.createPerson(entity);

        return ResponseEntity
                .created(URI.create("/persons/" + saved.getId()))
                .body(toResponse(saved));
    }

    // ✅ 4. Person güncelle
    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDto> updatePerson(
            @PathVariable Long id,
            @RequestBody PersonRequestDto req) {

        PersonEntity incoming = toEntity(req);
        Optional<PersonEntity> updated = personService.updatePerson(id, incoming);

        return updated.map(e -> ResponseEntity.ok(toResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ 5. Person sil (soft delete veya hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    // 🔄 Helper: Entity → Response DTO
    private PersonResponseDto toResponse(PersonEntity e) {
        return PersonResponseDto.builder()
                .id(e.getId())
                .name(e.getName())
                .surname(e.getSurname())
                .email(e.getEmail())
                .phone(e.getPhone())
                .active(e.getActive())
                .departmentId(e.getDepartmentId())
                .build();
    }

    // 🔄 Helper: Request DTO → Entity
    private PersonEntity toEntity(PersonRequestDto r) {
        return PersonEntity.builder()
                .name(r.getName())
                .surname(r.getSurname())
                .email(r.getEmail())
                .phone(r.getPhone())
                .departmentId(r.getDepartmentId())
                .active(true) // yeni kayıt aktif başlar
                .build();
    }
}
