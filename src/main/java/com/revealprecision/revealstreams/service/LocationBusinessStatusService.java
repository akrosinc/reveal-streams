package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.constants.LocationConstants;
import com.revealprecision.revealstreams.enums.PlanInterventionTypeEnum;
import com.revealprecision.revealstreams.persistence.domain.LiteStructureCount;
import com.revealprecision.revealstreams.persistence.domain.LocationCounts;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.TaskBusinessStateTracker;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.persistence.repository.LiteStructureCountRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationAboveStructureCountsRepository;
import com.revealprecision.revealstreams.persistence.repository.LocationCountsRepository;
import com.revealprecision.revealstreams.persistence.repository.TaskBusinessStateTrackerRepository;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationBusinessStatusService {

  private final LocationCountsRepository locationCountsRepository;
  private final TaskBusinessStateTrackerRepository taskBusinessStateTrackerRepository;
  private final LocationAboveStructureCountsRepository locationAboveStructureCountsRepository;
  private final LiteStructureCountRepository liteStructureCountRepository;

  public Long getLocationCountsForGeoLevelByHierarchyLocationParent(UUID parentLocationIdentifier,
      UUID locationHierarchyIdentifier, String geographicLevelName, Plan plan) {

    if ((plan.getInterventionType().getCode().equals(PlanInterventionTypeEnum.IRS_LITE.name())
        || plan.getInterventionType().getCode().equals(PlanInterventionTypeEnum.MDA_LITE.name()))
        && geographicLevelName.equals(
        LocationConstants.STRUCTURE)) {
      LiteStructureCount liteStructureCount = liteStructureCountRepository.findByParentLocationIdentifierAndLocationHierarchyIdentifier(
          parentLocationIdentifier, locationHierarchyIdentifier);

      if (liteStructureCount != null) {
        return (long) liteStructureCount.getStructureCounts();
      }

    } else {
      log.trace("parms passed: {} {} {}", parentLocationIdentifier, locationHierarchyIdentifier,
          geographicLevelName);
      LocationCounts locationCounts = locationCountsRepository.findLocationCountsByParentLocationIdentifierAndLocationHierarchyIdentifierAndGeographicLevelName(
          parentLocationIdentifier, locationHierarchyIdentifier, geographicLevelName);
      if (locationCounts != null) {
        log.trace("result: {} {} {} {} {} {} {} ", parentLocationIdentifier,
            locationHierarchyIdentifier, geographicLevelName,
            locationCounts.getParentLocationIdentifier(), locationCounts.getLocationCount(),
            locationCounts.getParentLocationName(), locationCounts.getParentGeographicLevelName());
        return locationCounts.getLocationCount();
      }
    }
    return null;
  }

  public Long getLocationCountsForGeoLevelByHierarchyLocationParentForWaterBodies(
      UUID parentLocationIdentifier,
      UUID locationHierarchyIdentifier, String geographicLevelName, Plan plan) {

    log.trace("parms passed: {} {} {}", parentLocationIdentifier, locationHierarchyIdentifier,
        geographicLevelName);
    LocationCounts locationCounts = locationCountsRepository.findLocationCountsByParentLocationIdentifierAndLocationHierarchyIdentifierAndGeographicLevelName(
        parentLocationIdentifier, locationHierarchyIdentifier, geographicLevelName);
    if (locationCounts != null) {
      log.trace("result: {} {} {} {} {} {} {} ", parentLocationIdentifier,
          locationHierarchyIdentifier, geographicLevelName,
          locationCounts.getParentLocationIdentifier(), locationCounts.getLocationCount(),
          locationCounts.getParentLocationName(), locationCounts.getParentGeographicLevelName());
      return locationCounts.getWaterBodyCount();
    }

    return null;
  }

  public Long getLocationCountsForGeoLevelByHierarchyLocationParentForNonWaterBodies(
      UUID parentLocationIdentifier,
      UUID locationHierarchyIdentifier, String geographicLevelName, Plan plan) {

    log.trace("parms passed: {} {} {}", parentLocationIdentifier, locationHierarchyIdentifier,
        geographicLevelName);
    LocationCounts locationCounts = locationCountsRepository.findLocationCountsByParentLocationIdentifierAndLocationHierarchyIdentifierAndGeographicLevelName(
        parentLocationIdentifier, locationHierarchyIdentifier, geographicLevelName);
    if (locationCounts != null) {
      log.trace("result: {} {} {} {} {} {} {} ", parentLocationIdentifier,
          locationHierarchyIdentifier, geographicLevelName,
          locationCounts.getParentLocationIdentifier(), locationCounts.getLocationCount(),
          locationCounts.getParentLocationName(), locationCounts.getParentGeographicLevelName());
      return locationCounts.getLocationWithoutSurveyLocationType();
    }

    return null;
  }

  public LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      String taskBusinessStatus, UUID locationHierarchyIdentifier) {
    return taskBusinessStateTrackerRepository.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(
        planIdentifier, parentLocationIdentifier, taskLocationGeographicLevelName,
        taskBusinessStatus, locationHierarchyIdentifier);
  }


  public long getTotalLocationsByParentAndPlan(UUID planIdentifier,
      UUID parentLocationIdentifier) {
    return taskBusinessStateTrackerRepository.getTotalLocationsByParentAndPlan(planIdentifier,
        parentLocationIdentifier);
  }

  public LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForNonWaterBodies(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      String taskBusinessStatus, UUID locationHierarchyIdentifier) {
    return taskBusinessStateTrackerRepository.getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForNonWaterBodies(
        planIdentifier, parentLocationIdentifier, taskLocationGeographicLevelName,
        taskBusinessStatus, locationHierarchyIdentifier);
  }

  public LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForWaterBodies(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      String taskBusinessStatus, UUID locationHierarchyIdentifier) {
    return taskBusinessStateTrackerRepository.getLocationBusinessStateObjPerBusinessStatusAndGeoLevelForWaterBodies(
        planIdentifier, parentLocationIdentifier, taskLocationGeographicLevelName,
        taskBusinessStatus, locationHierarchyIdentifier);
  }

  public TaskBusinessStateTracker findLocationBusinessState(UUID locationHierarchyIdentifier,
      UUID taskLocationIdentifier, UUID planIdentifier) {
    return taskBusinessStateTrackerRepository.findDistinctTaskBusinessStateTrackerByLocationHierarchyIdentifierAndTaskLocationIdentifierAndPlanIdentifier(
        locationHierarchyIdentifier, taskLocationIdentifier, planIdentifier);
  }


  public Long getCountsOfVisitedLocationAboveStructure(UUID locationHierarchyIdentifier,
      UUID parentLocationIdentifier, UUID planIdentifier, String childGeographicLevelName) {
    return locationAboveStructureCountsRepository.getCountOfVisitedLocations(
        locationHierarchyIdentifier, parentLocationIdentifier, planIdentifier,
        childGeographicLevelName);
  }

  public Long getCountsOfVisitedEffectivelyLocationAboveStructure(UUID locationHierarchyIdentifier,
      UUID parentLocationIdentifier, UUID planIdentifier, String childGeographicLevelName) {
    return locationAboveStructureCountsRepository.getCountOfVisitedEffectivelyLocations(
        locationHierarchyIdentifier, parentLocationIdentifier, planIdentifier,
        childGeographicLevelName);
  }

  public Long getCountsOfTreatedLocationAboveStructure(UUID locationHierarchyIdentifier,
      UUID parentLocationIdentifier, UUID planIdentifier, String childGeographicLevelName) {
    return locationAboveStructureCountsRepository.getCountOfTreatedLocations(
        locationHierarchyIdentifier, parentLocationIdentifier, planIdentifier,
        childGeographicLevelName);
  }

  public Map<String, LocationBusinessStateCount> getLocationBusinessStateObjPerGeoLevel(
      UUID planIdentifier, UUID parentLocationIdentifier, String taskLocationGeographicLevelName,
      UUID locationHierarchyIdentifier) {

    log.debug("planIdentifier: {} parentLocationIdentifier: {}, locationHierarchyIdentifier: {}",
        planIdentifier, parentLocationIdentifier, locationHierarchyIdentifier);

    Set<LocationBusinessStateCount> locationBusinessStateObjPerGeoLevel = taskBusinessStateTrackerRepository.getLocationBusinessStateObjPerGeoLevel(
        planIdentifier, parentLocationIdentifier, LocationConstants.STRUCTURE,
        locationHierarchyIdentifier);

    locationBusinessStateObjPerGeoLevel.forEach(
        locationBusinessStateCount -> log.debug("{} - locationBusinessStateObjPerGeoLevel({})",
            parentLocationIdentifier, locationBusinessStateCount.getTaskBusinessStatus()));

    return locationBusinessStateObjPerGeoLevel.stream().collect(
        Collectors.toMap(LocationBusinessStateCount::getTaskBusinessStatus,
            locationBusinessStateCount -> locationBusinessStateCount, (a, b) -> b));

  }
}
