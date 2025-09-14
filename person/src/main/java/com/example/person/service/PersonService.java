package com.example.person.service;

import com.example.person.entity.PersonEntity;
import java.util.List;
import java.util.Optional;

public interface PersonService {
    // Tüm person kayıtlarını getirir
    List<PersonEntity> getAllPersons();
    // Id ile mevcut person kaydı getirir
    Optional<PersonEntity> getPersonById(Long id);
    // Person kaydı oluşturur
    PersonEntity createPerson(PersonEntity person);
    // Mevcut bir person'ı günceller
    Optional<PersonEntity> updatePerson(Long id, PersonEntity person);
    //Id'ye göre mevcut bir person kaydını siler
    void deletePerson(Long id);
    // --- Admin ihtiyacı ---
    List<PersonEntity> getUnassignedPersons();
    List<PersonEntity> getPersonsByDepartment(Long departmentId);
    Optional<PersonEntity> assignDepartment(Long personId, Long departmentId);
    // email ile person bul ---
    Optional<PersonEntity> getByEmail(String email);
}
