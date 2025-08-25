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
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public ResponseEntity<List<PersonResponseDto>> getAllPersons() {
        List<PersonResponseDto> out = personService.getAllPersons()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDto> getPersonById(@PathVariable Long id) {
        Optional<PersonEntity> opt = personService.getPersonById(id);
        return opt.map(e -> ResponseEntity.ok(toResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ user-service buraya POST atar
    @PostMapping
    public ResponseEntity<PersonResponseDto> createPerson(@RequestBody PersonRequestDto req) {
        PersonEntity entity = toEntity(req);
        entity.setActive(true); // ✅ EKLENDİ
        PersonEntity saved = personService.createPerson(entity);
        return ResponseEntity.created(URI.create("/api/persons/" + saved.getId()))
                .body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDto> updatePerson(@PathVariable Long id,
                                                          @RequestBody PersonRequestDto req) {
        PersonEntity in = toEntity(req);
        return personService.updatePerson(id, in)
                .map(e -> ResponseEntity.ok(toResponse(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    // helpers
    private PersonEntity toEntity(PersonRequestDto r) {
        return PersonEntity.builder()
                .name(r.getName())
                .surname(r.getSurname())
                .email(r.getEmail())
                .phone(r.getPhone())
                .departmentId(r.getDepartmentId()) // ✅ null olabilir
                .build();
    }
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
