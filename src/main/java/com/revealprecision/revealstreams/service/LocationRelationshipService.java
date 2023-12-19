package com.revealprecision.revealstreams.service;


import com.revealprecision.revealstreams.dto.PlanLocationDetails;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.LocationHierarchy;
import com.revealprecision.revealstreams.persistence.domain.LocationRelationship;
import com.revealprecision.revealstreams.persistence.projection.LocationChildrenCountProjection;
import com.revealprecision.revealstreams.persistence.repository.LocationRelationshipRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LocationRelationshipService {

  private final LocationRelationshipRepository locationRelationshipRepository;
  @Autowired
  public LocationRelationshipService(LocationRelationshipRepository locationRelationshipRepository) {
    this.locationRelationshipRepository = locationRelationshipRepository;

  }

  public List<LocationChildrenCountProjection> getLocationChildrenCount(
      UUID locationHierarchyIdentifier) {
    return locationRelationshipRepository.getLocationChildrenCount(locationHierarchyIdentifier);
  }

  public List<PlanLocationDetails> getLocationChildrenByLocationParentIdentifierAndPlanIdentifier(
      UUID parentLocationIdentifiers, UUID planIdentifier) {

    return locationRelationshipRepository.getLocationDetailsByParentIdAndPlanId(
        parentLocationIdentifiers, planIdentifier);
  }

  public PlanLocationDetails getRootLocationDetailsByPlanId(UUID planIdentifier) {
    return locationRelationshipRepository.getRootLocationDetailsByAndPlanId(planIdentifier);
  }

  public List<PlanLocationDetails> getRootLocationsDetailsByPlanId(UUID planIdentifier) {
    return locationRelationshipRepository.getRootLocationsDetailByAndPlanId(planIdentifier);
  }

  public List<LocationChildrenCountProjection> getLocationAssignedChildrenCount(
      UUID locationHierarchyIdentifier, UUID planIdentifier) {
    return locationRelationshipRepository.getLocationAssignedChildrenCount(
        locationHierarchyIdentifier, planIdentifier);
  }
  public List<PlanLocationDetails> getAssignedLocationChildrenByLocationParentIdentifierAndPlanIdentifier(
      UUID parentLocationIdentifiers, UUID planIdentifier) {
    return locationRelationshipRepository.getAssignedLocationDetailsByParentIdAndPlanId(
        parentLocationIdentifiers, planIdentifier);
  }
  public Long getNumberOfChildrenByGeoLevelNameWithinLocationAndHierarchy(
      String geographicLevelName
      , UUID locationIdentifier,
      UUID locationHierarchyIdentifier) {

    return locationRelationshipRepository.getNumberOfChildrenByGeoLevelNameWithinLocationAndHierarchy(
        geographicLevelName
        , locationIdentifier.toString(), locationHierarchyIdentifier);

  }



  public LocationRelationship getLocationRelationshipsForLocation(
      UUID locationHierarchyIdentifier, UUID locationIdentifier) {
    return locationRelationshipRepository.getLocationRelationshipByLocation_IdentifierAndLocationHierarchy_Identifier(
        locationIdentifier, locationHierarchyIdentifier);
  }


  public List<Location> getChildrenLocations(UUID hierarchyIdentifier, UUID locationIdentifier) {
    List<Location> children = locationRelationshipRepository.getChildren(hierarchyIdentifier,
        locationIdentifier);
    return children;
  }

  public Location getLocationParent(Location location, LocationHierarchy locationHierarchy) {
    return locationRelationshipRepository.getParentLocationByLocationIdAndHierarchyId(
        location.getIdentifier(), locationHierarchy.getIdentifier());
  }

  public Location getLocationParentByLocationIdentifierAndHierarchyIdentifier(UUID locationIdentifier, UUID locationHierarchyIdentifier) {
    return locationRelationshipRepository.getParentLocationByLocationIdAndHierarchyId(
        locationIdentifier, locationHierarchyIdentifier);
  }

}
