package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.repository.LocationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationRepository locationRepository;
  private final LocationRelationshipService locationRelationshipService;




  public Location findByIdentifier(UUID identifier) {
    return locationRepository.findById(identifier).orElseThrow(
        () -> new NotFoundException(Pair.of(Location.Fields.identifier, identifier),
            Location.class));
  }



  public Location getLocationParentByLocationIdentifierAndHierarchyIdentifier(
      UUID locationIdentifier, UUID locationHierarchyIdentifier) {
    return locationRelationshipService.getLocationParentByLocationIdentifierAndHierarchyIdentifier(
        locationIdentifier, locationHierarchyIdentifier);
  }



  public List<Location> getLocationsByPeople(UUID personIdentifier) {
    return locationRepository.getLocationsByPeople_Identifier(personIdentifier);
  }


}
