package com.revealprecision.revealstreams.service;


import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.persistence.domain.User;
import com.revealprecision.revealstreams.persistence.repository.UserRepository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.revealprecision.revealstreams.persistence.domain.User.Fields;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getByKeycloakId(UUID id) {
    return userRepository.findBySid(id)
        .orElseThrow(() -> new NotFoundException(Pair.of(Fields.sid, id), User.class));
  }
}
