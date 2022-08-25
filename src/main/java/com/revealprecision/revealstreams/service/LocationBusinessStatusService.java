package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.persistence.domain.LocationCounts;
import com.revealprecision.revealstreams.persistence.domain.TaskBusinessStateTracker;
import com.revealprecision.revealstreams.persistence.projection.LocationBusinessStateCount;
import com.revealprecision.revealstreams.persistence.repository.LocationCountsRepository;
import com.revealprecision.revealstreams.persistence.repository.TaskBusinessStateTrackerRepository;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationBusinessStatusService {

  private final LocationCountsRepository locationCountsRepository;
  private final TaskBusinessStateTrackerRepository taskBusinessStateTrackerRepository;
  private final JdbcTemplate jdbcTemplate;

  public LocationCounts getLocationCountsForGeoLevelByHierarchyLocationParent(
      UUID parentLocationIdentifier, UUID locationHierarchyIdentifier, String geographicLevelName) {

    log.trace("parms passed: {} {} {}",parentLocationIdentifier, locationHierarchyIdentifier, geographicLevelName);
    LocationCounts locationCounts = locationCountsRepository.findLocationCountsByParentLocationIdentifierAndLocationHierarchyIdentifierAndGeographicLevelName(
        parentLocationIdentifier, locationHierarchyIdentifier, geographicLevelName);
    if (locationCounts != null) {
      log.trace("result: {} {} {} {} {} {} {} ", parentLocationIdentifier,
          locationHierarchyIdentifier, geographicLevelName,
          locationCounts.getParentLocationIdentifier(),
          locationCounts.getLocationCount(), locationCounts.getParentLocationName(),
          locationCounts.getParentGeographicLevelName());
    }
    return locationCounts;
  }


  public Set<LocationBusinessStateCount> getLocationBusinessStateObjPerGeoLevel(UUID planIdentifier, UUID parentLocationIdentifier,String taskLocationGeographicLevelName){
    return taskBusinessStateTrackerRepository.getLocationBusinessStateObjPerGeoLevel(planIdentifier, parentLocationIdentifier,taskLocationGeographicLevelName);
  }

  public LocationBusinessStateCount getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(UUID planIdentifier, UUID parentLocationIdentifier,String taskLocationGeographicLevelName, String taskBusinessStatus,UUID locationHierarchyIdentifier){
    return taskBusinessStateTrackerRepository.getLocationBusinessStateObjPerBusinessStatusAndGeoLevel(planIdentifier, parentLocationIdentifier,taskLocationGeographicLevelName, taskBusinessStatus, locationHierarchyIdentifier);
  }

  public TaskBusinessStateTracker findLocationBusinessState( UUID locationHierarchyIdentifier, UUID taskLocationIdentifier, UUID planIdentifier){
    return taskBusinessStateTrackerRepository.findTaskBusinessStateTrackerByLocationHierarchyIdentifierAndTaskLocationIdentifierAndPlanIdentifier( locationHierarchyIdentifier, taskLocationIdentifier, planIdentifier);
  }


}
