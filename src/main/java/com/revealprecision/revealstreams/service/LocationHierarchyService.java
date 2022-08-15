package com.revealprecision.revealstreams.service;

import static java.util.stream.Collectors.joining;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.revealprecision.revealstreams.persistence.domain.LocationHierarchy;
import com.revealprecision.revealstreams.persistence.repository.LocationHierarchyRepository;

@Service
@RequiredArgsConstructor
public class LocationHierarchyService {

  private final LocationHierarchyRepository locationHierarchyRepository;

  public List<LocationHierarchy> getAll() {
    return locationHierarchyRepository
        .findAll();
  }

}
