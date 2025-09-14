package com.example.person.controller;

import com.example.person.dto.request.PersonRequestDto;
import com.example.person.dto.response.PersonResponseDto;
import com.example.person.entity.PersonEntity;
import com.example.person.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;

    // Giriş yapan PERSON’un bilgilerini döner
    @GetMapping("/me")
    public ResponseEntity<PersonResponseDto> getMe(Authentication auth) {
        String email = auth.getName();
        Optional<PersonEntity> opt = personService.getByEmail(email);
        return opt.map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDto>> getAllPersons() {
        List<PersonResponseDto> out = personService.getAllPersons()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDto> getPersonById(@PathVariable Long id) {
        return personService.getPersonById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // sadece Person kaydı yapar (login yok)
    @PostMapping
    public ResponseEntity<PersonResponseDto> createPerson(@RequestBody PersonRequestDto req) {
        PersonEntity entity = toEntity(req);
        entity.setActive(true);
        PersonEntity saved = personService.createPerson(entity);
        return ResponseEntity.created(URI.create("/api/persons/" + saved.getId()))
                .body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDto> updatePerson(@PathVariable Long id,
                                                          @RequestBody PersonRequestDto req) {
        PersonEntity in = toEntity(req);
        return personService.updatePerson(id, in)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    // Request DTO'yu Entity'e dönüştürür.
    private PersonEntity toEntity(PersonRequestDto r) {
        return PersonEntity.builder()
                .name(r.getName())
                .surname(r.getSurname())
                .email(r.getEmail())
                .phone(r.getPhone())
                .departmentId(r.getDepartmentId())
                .build();
    }

    // Entity'yi Response DTO'ya dönüştürür.
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
}
