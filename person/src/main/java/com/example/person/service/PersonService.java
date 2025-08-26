package com.example.person.service;

import com.example.person.entity.PersonEntity;

import java.util.List;
import java.util.Optional;

public interface PersonService {

    List<PersonEntity> getAllPersons();
    Optional<PersonEntity> getPersonById(Long id);
    PersonEntity createPerson(PersonEntity person);
    Optional<PersonEntity> updatePerson(Long id, PersonEntity person);
    void deletePerson(Long id);

    // --- Admin ihtiyacı ---
    List<PersonEntity> getUnassignedPersons();
    List<PersonEntity> getPersonsByDepartment(Long departmentId);
    Optional<PersonEntity> assignDepartment(Long personId, Long departmentId);

    // --- Yeni ekledik: email ile person bul ---
    Optional<PersonEntity> getByEmail(String email);
}
