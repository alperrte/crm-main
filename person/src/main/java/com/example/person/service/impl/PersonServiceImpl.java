package com.example.person.service.impl;

import com.example.person.entity.PersonEntity;
import com.example.person.repository.PersonRepository;
import com.example.person.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public List<PersonEntity> getAllPersons() {
        return personRepository.findByActiveTrue();
    }

    @Override
    public Optional<PersonEntity> getPersonById(Long id) {
        return personRepository.findById(id)
                .filter(PersonEntity::getActive); // sadece aktif kayıt döner
    }

    @Override
    public PersonEntity createPerson(PersonEntity person) {
        person.setActive(true); // varsayılan olarak aktif
        return personRepository.save(person);
    }

    @Override
    public Optional<PersonEntity> updatePerson(Long id, PersonEntity updatedPerson) {
        return personRepository.findById(id)
                .filter(PersonEntity::getActive)
                .map(existingPerson -> {
                    existingPerson.setName(updatedPerson.getName());
                    existingPerson.setSurname(updatedPerson.getSurname());
                    existingPerson.setEmail(updatedPerson.getEmail());
                    existingPerson.setPhone(updatedPerson.getPhone());
                    existingPerson.setDepartmentId(updatedPerson.getDepartmentId());
                    return personRepository.save(existingPerson);
                });
    }

    @Override
    public void deletePerson(Long id) {
        personRepository.findById(id)
                .filter(PersonEntity::getActive)
                .ifPresent(person -> {
                    person.setActive(false); // soft delete mantığı
                    personRepository.save(person);
                });


    }
}
