package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.enums.PlanInterventionTypeEnum;
import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.models.LocationNode;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.projection.LocationChildrenCountProjection;
import com.revealprecision.revealstreams.persistence.projection.LocationFlat;
import com.revealprecision.revealstreams.persistence.repository.LocationRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationRepository locationRepository;
  private final LocationRelationshipService locationRelationshipService;
  private final PlanService planService;

  @Transactional(readOnly = true)
  public List<LocationNode> buildTree() {

    Map<UUID, LocationNode> map = new HashMap<>();

    // STREAM FROM DB
    try (Stream<LocationFlat> stream = locationRepository.streamAllFlat()) {

      stream.forEach(row -> {
        UUID id = row.getLocationIdentifier();

        LocationNode node = map.computeIfAbsent(id, k ->
            new LocationNode(
                k,
                row.getParentIdentifier(),
                row.getName(),
                row.getGeoLevelName()
            )
        );

        // ensure parentId is set
        node.setParentId( row.getParentIdentifier());
      });
    }

    // LINK PARENTS + CHILDREN
    List<LocationNode> roots = new ArrayList<>();

    for (LocationNode node : map.values()) {
      if (node.getParentId() == null) {
        roots.add(node);
      } else {
        LocationNode parent = map.get(node.getParentId());

        if (parent != null) {
          parent.addChild(node);
        } else {
          // fallback if parent missing
          roots.add(node);
        }
      }
    }

    return roots;
  }

  public List<PlanLocationDetails> getAssignedLocationsByParentIdentifierAndPlanIdentifier(
      UUID parentIdentifier,
      UUID planIdentifier,
      boolean isBeforeStructure) {
    Plan plan = planService.findPlanByIdentifier(planIdentifier);
    Location location = findByIdentifier(parentIdentifier);
    Map<UUID, Long> childrenCount;
    if (!isBeforeStructure) {
      childrenCount = locationRelationshipService.getLocationAssignedChildrenCount(
              plan.getLocationHierarchy().getIdentifier(), planIdentifier)
          .stream().filter(loc -> loc.getParentIdentifier() != null)
          .collect(Collectors.toMap(loc -> UUID.fromString(loc.getParentIdentifier()),
              LocationChildrenCountProjection::getChildrenCount));
    } else {

      childrenCount = locationRelationshipService.getLocationChildrenCount(
              plan.getLocationHierarchy().getIdentifier())
          .stream().filter(loc -> loc.getParentIdentifier() != null)
          .collect(Collectors.toMap(loc -> UUID.fromString(loc.getParentIdentifier()),
              LocationChildrenCountProjection::getChildrenCount));
    }
    List<PlanLocationDetails> response = locationRelationshipService
        .getAssignedLocationChildrenByLocationParentIdentifierAndPlanIdentifier(
            parentIdentifier, planIdentifier);
    response.forEach(resp -> resp.setChildrenNumber(
        childrenCount.containsKey(resp.getLocation().getIdentifier()) ? childrenCount.get(
            resp.getLocation().getIdentifier()) : 0));
    if (location != null) {
      response = response.stream()
          .peek(planLocationDetail -> planLocationDetail.setParentLocation(location))
          .collect(Collectors.toList());
    }
    return response;
  }

  public Location findByIdentifier(UUID identifier) {
    return locationRepository.findById(identifier).orElseThrow(
        () -> new NotFoundException(Pair.of(Location.Fields.identifier, identifier),
            Location.class));
  }
  public List<Location> findByListOfIdentifiers(List<UUID> identifier) {
    return locationRepository.findAllByIdentifierIn(identifier).orElseThrow(
        () -> new NotFoundException(Pair.of(Location.Fields.identifier, identifier),
            Location.class));
  }

  public List<PlanLocationDetails> getLocationsByParentIdentifierAndPlanIdentifier(
      UUID parentIdentifier,
      UUID planIdentifier) {
    Plan plan = planService.findPlanByIdentifier(planIdentifier);
    Location location = findByIdentifier(parentIdentifier);

    // if location is at plan target level do not load child location
    if (plan.getInterventionType().getCode().equals(PlanInterventionTypeEnum.MDA_LITE.name())) {
      PlanLocationDetails planLocationDetails = new PlanLocationDetails();
      planLocationDetails.setLocation(location);

      return List.of(planLocationDetails);
    } else {
      if (Objects.equals(plan.getPlanTargetType().getGeographicLevel().getName(),
          location.getGeographicLevel().getName())) {
        throw new NotFoundException("Child location is not in plan target level");
      }
    }

    Map<UUID, Long> childrenCount = locationRelationshipService.getLocationChildrenCount(
            plan.getLocationHierarchy().getIdentifier())
        .stream().filter(loc -> loc.getParentIdentifier() != null)
        .collect(Collectors.toMap(loc -> UUID.fromString(loc.getParentIdentifier()),
            LocationChildrenCountProjection::getChildrenCount));
    List<PlanLocationDetails> response = locationRelationshipService
        .getLocationChildrenByLocationParentIdentifierAndPlanIdentifier(
            parentIdentifier, planIdentifier);
    response.forEach(resp -> resp.setChildrenNumber(
        childrenCount.containsKey(resp.getLocation().getIdentifier()) ? childrenCount.get(
            resp.getLocation().getIdentifier()) : 0));
    if (location != null) {
      response = response.stream()
          .peek(planLocationDetail -> planLocationDetail.setParentLocation(location))
          .collect(Collectors.toList());
    }
    return response;
  }

  public PlanLocationDetails getRootLocationByPlanIdentifier(UUID planIdentifier) {
    Plan plan = planService.findPlanByIdentifier(planIdentifier);
    Map<UUID, Long> childrenCount = locationRelationshipService.getLocationChildrenCount(
            plan.getLocationHierarchy().getIdentifier())
        .stream().filter(loc -> loc.getParentIdentifier() != null)
        .collect(Collectors.toMap(loc -> UUID.fromString(loc.getParentIdentifier()),
            LocationChildrenCountProjection::getChildrenCount));
    PlanLocationDetails response = locationRelationshipService.getRootLocationDetailsByPlanId(
        planIdentifier);
    response.setChildrenNumber(
        childrenCount.containsKey(response.getLocation().getIdentifier()) ? childrenCount.get(
            response.getLocation().getIdentifier()) : 0);
    return response;
  }

  public List<PlanLocationDetails> getRootLocationsByPlanIdentifier(UUID planIdentifier) {
    Plan plan = planService.findPlanByIdentifier(planIdentifier);
    Map<UUID, Long> childrenCount = locationRelationshipService.getLocationChildrenCount(
            plan.getLocationHierarchy().getIdentifier())
        .stream().filter(loc -> loc.getParentIdentifier() != null)
        .collect(Collectors.toMap(loc -> UUID.fromString(loc.getParentIdentifier()),
            LocationChildrenCountProjection::getChildrenCount));
    List<PlanLocationDetails> response = locationRelationshipService.getRootLocationsDetailsByPlanId(
        planIdentifier);
    response.stream().peek( locationdetail -> locationdetail.setChildrenNumber(
        childrenCount.containsKey(locationdetail.getLocation().getIdentifier()) ? childrenCount.get(
            locationdetail.getLocation().getIdentifier()) : 0));
    return response;
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
