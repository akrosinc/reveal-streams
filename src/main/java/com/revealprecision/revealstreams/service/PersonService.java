package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.persistence.domain.Person;
import com.revealprecision.revealstreams.persistence.domain.Person.Fields;
import com.revealprecision.revealstreams.persistence.repository.PersonRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PersonService {

  final PersonRepository personRepository;


  @Autowired
  public PersonService(PersonRepository personRepository) {
    this.personRepository = personRepository;

  }

  public Person getPersonByIdentifier(UUID personIdentifier) {
    return personRepository.findByIdentifier(personIdentifier).orElseThrow(
        () -> new NotFoundException(Pair.of(Fields.identifier, personIdentifier), Person.class));
  }


}
